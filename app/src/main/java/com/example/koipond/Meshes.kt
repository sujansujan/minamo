package com.example.koipond

import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class GlMesh(
    val vao: Int,
    private val vbo: Int,
    private val ibo: Int,
    val indexCount: Int
) {
    fun draw() {
        GLES30.glBindVertexArray(vao)
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, indexCount, GLES30.GL_UNSIGNED_SHORT, 0)
    }

    fun release() {
        GLES30.glDeleteBuffers(2, intArrayOf(vbo, ibo), 0)
        GLES30.glDeleteVertexArrays(1, intArrayOf(vao), 0)
    }
}

object Meshes {

    /**
     * Fullscreen quad [-1, 1] with UV [0, 1].
     * Vertex layout: [posX, posY, u, v] (4 floats)
     */
    fun createFullscreenQuad(): GlMesh {
        val vertices = floatArrayOf(
            -1f, -1f, 0f, 0f,
             1f, -1f, 1f, 0f,
            -1f,  1f, 0f, 1f,
             1f,  1f, 1f, 1f
        )
        val indices = shortArrayOf(0, 1, 2, 2, 1, 3)
        return buildMesh(vertices, indices, intArrayOf(2, 2))
    }

    /**
     * Subdivided ribbon mesh for swaying underwater vallisneria / kelp blades.
     * u in [0, 1] across blade width, v in [0, 1] from root to tip.
     */
    fun createVallisneriaRibbon(segmentsY: Int = 22): GlMesh {
        val verts = FloatArray((segmentsY + 1) * 2 * 4)
        var vIdx = 0
        for (i in 0..segmentsY) {
            val v = i.toFloat() / segmentsY.toFloat()
            val halfWidth = (1f - v.pow(1.45f) * 0.80f).coerceAtLeast(0.14f)
            verts[vIdx++] = -halfWidth
            verts[vIdx++] = v
            verts[vIdx++] = 0f
            verts[vIdx++] = v

            verts[vIdx++] = halfWidth
            verts[vIdx++] = v
            verts[vIdx++] = 1f
            verts[vIdx++] = v
        }

        val indices = ShortArray(segmentsY * 6)
        var iIdx = 0
        for (i in 0 until segmentsY) {
            val row0 = (i * 2).toShort()
            val row1 = ((i + 1) * 2).toShort()
            indices[iIdx++] = row0
            indices[iIdx++] = (row0 + 1).toShort()
            indices[iIdx++] = row1
            indices[iIdx++] = row1
            indices[iIdx++] = (row0 + 1).toShort()
            indices[iIdx++] = (row1 + 1).toShort()
        }
        return buildMesh(verts, indices, intArrayOf(2, 2))
    }

    /**
     * Builds a 3D side-profile + oblique-perspective fish mesh designed for an underwater split-view camera.
     * In local fish coordinates:
     *   +X = forward snout (spineT = 0.0) to -X = tail tip (spineT = 1.25)
     *   +Y = dorsal (top) to -Y = ventral (belly)
     *   +Z = right flank facing camera, -Z = left flank
     *
     * Vertex layout (9 floats):
     *   aPos:    vec3 (x, y, z)
     *   aNormal: vec3 (nx, ny, nz)
     *   aUvPart: vec3 (vHeight [0=belly..1=back], spineT [0=nose..1.25=tail], partId)
     *     where partId: 0.0 = body, 1.0 = pectoral fin, 2.0 = dorsal/ventral fin, 3.0 = caudal tail fin
     */
    fun createFishMesh(spineSegments: Int = 28, radialSegments: Int = 12): GlMesh {
        val vertexList = ArrayList<Float>(4096)
        val indexList = ArrayList<Short>(8192)

        fun addVertex(
            x: Float, y: Float, z: Float,
            nx: Float, ny: Float, nz: Float,
            vHeight: Float, spineT: Float, partId: Float
        ): Short {
            val idx = (vertexList.size / 9).toShort()
            val len = sqrt(nx * nx + ny * ny + nz * nz).coerceAtLeast(1e-4f)
            vertexList.add(x)
            vertexList.add(y)
            vertexList.add(z)
            vertexList.add(nx / len)
            vertexList.add(ny / len)
            vertexList.add(nz / len)
            vertexList.add(vHeight)
            vertexList.add(spineT)
            vertexList.add(partId)
            return idx
        }

        // 1. Closed 3D fusiform body tube so the fish looks solid from any 3D yaw/pitch angle
        val stride = radialSegments + 1
        for (i in 0..spineSegments) {
            val t = i.toFloat() / spineSegments.toFloat() // 0 = snout, 1 = caudal peduncle
            val spineX = 0.50f - t * 0.92f

            // Base vertical body height (can be scaled per species in vertex shader)
            val heightProfile = if (t < 0.28f) {
                val nt = t / 0.28f
                0.24f * sqrt(nt.coerceAtLeast(0f)) * (0.78f + 0.22f * nt)
            } else {
                val nt = (t - 0.28f) / 0.72f
                0.24f * (1f - nt).pow(1.35f) + 0.042f * (1f - nt)
            }
            val thicknessProfile = heightProfile * 0.58f

            for (j in 0..radialSegments) {
                val rFrac = j.toFloat() / radialSegments.toFloat()
                val theta = rFrac * 2f * PI.toFloat() // Full 360 tube around spine
                val sy = cos(theta) // +1 at dorsal crest, -1 at belly
                val sz = sin(theta) // +1 facing camera, -1 away

                val y = sy * heightProfile
                val z = sz * thicknessProfile
                val nx = (0.25f - t) * 0.30f
                val ny = sy * 0.85f
                val nz = sz * 1.15f
                val vHeight = sy * 0.5f + 0.5f // 0 = belly, 1 = dorsal ridge

                addVertex(spineX, y, z, nx, ny, nz, vHeight, t, 0f)
            }
        }

        for (i in 0 until spineSegments) {
            for (j in 0 until radialSegments) {
                val r0 = (i * stride + j).toShort()
                val r1 = ((i + 1) * stride + j).toShort()
                indexList.add(r0)
                indexList.add(r1)
                indexList.add((r0 + 1).toShort())

                indexList.add((r0 + 1).toShort())
                indexList.add(r1)
                indexList.add((r1 + 1).toShort())
            }
        }

        // 2. Dorsal & Anal/Ventral fins (partId = 2.0)
        fun addRibbonFin(isDorsal: Boolean, startT: Float, endT: Float, maxArch: Float) {
            val segs = 12
            val baseIdx = vertexList.size / 9
            val signY = if (isDorsal) 1f else -1f
            for (k in 0..segs) {
                val f = k.toFloat() / segs.toFloat()
                val spineT = startT + f * (endT - startT)
                val x = 0.50f - spineT * 0.92f
                val bodyH = 0.22f * (1f - ((spineT - 0.25f).coerceAtLeast(0f) / 0.75f)).pow(1.2f)
                val arch = sin(f * PI.toFloat()).pow(0.75f) * maxArch

                // Fin root on body
                addVertex(
                    x, signY * bodyH * 0.88f, 0f,
                    0f, signY * 0.4f, 0.9f,
                    if (isDorsal) 1f else 0f, spineT, 2f
                )
                // Swept fin tip
                addVertex(
                    x - 0.08f * f, signY * (bodyH * 0.88f + arch), 0.01f,
                    -0.15f, signY * 0.5f, 0.85f,
                    if (isDorsal) 1f else 0f, (spineT + 0.08f).coerceAtMost(1.15f), 2f
                )
            }
            for (k in 0 until segs) {
                val v0 = (baseIdx + k * 2).toShort()
                val v1 = (baseIdx + (k + 1) * 2).toShort()
                indexList.add(v0)
                indexList.add((v0 + 1).toShort())
                indexList.add(v1)
                indexList.add(v1)
                indexList.add((v0 + 1).toShort())
                indexList.add((v1 + 1).toShort())
            }
        }

        addRibbonFin(isDorsal = true, startT = 0.28f, endT = 0.76f, maxArch = 0.24f)
        addRibbonFin(isDorsal = false, startT = 0.52f, endT = 0.82f, maxArch = 0.18f)

        // 3. Pectoral fins (left and right flanks, partId = 1.0)
        fun addPectoralFin(sideZ: Float) {
            val fanSegs = 7
            val rootIdx = addVertex(
                0.22f, -0.06f, sideZ * 0.11f,
                0f, -0.2f, sideZ,
                0.5f, 0.26f, 1f
            )
            val ringStart = vertexList.size / 9
            for (k in 0..fanSegs) {
                val f = k.toFloat() / fanSegs.toFloat()
                val ang = -0.35f + f * 1.15f
                val fx = 0.22f - cos(ang) * 0.18f
                val fy = -0.06f - sin(ang) * 0.14f
                val fz = sideZ * (0.11f + 0.09f * sin(f * PI.toFloat()))
                addVertex(
                    fx, fy, fz,
                    -0.2f, -0.2f, sideZ * 0.95f,
                    f, 0.32f + f * 0.08f, 1f
                )
            }
            for (k in 0 until fanSegs) {
                val v0 = (ringStart + k).toShort()
                val v1 = (ringStart + k + 1).toShort()
                indexList.add(rootIdx)
                indexList.add(v0)
                indexList.add(v1)
            }
        }
        addPectoralFin(+1f)
        addPectoralFin(-1f)

        // 4. Flowing Forked / Veil Caudal Tail Fin (partId = 3.0)
        val tailRows = 10
        val tailCols = 8
        val tailBase = vertexList.size / 9
        for (r in 0..tailRows) {
            val rf = r.toFloat() / tailRows.toFloat() // 0 at peduncle, 1 at tail tip
            val spineT = 0.90f + rf * 0.35f
            val baseX = -0.40f - rf * 0.34f
            val spreadY = 0.05f + rf * 0.30f
            for (c in 0..tailCols) {
                val cf = c.toFloat() / tailCols.toFloat() // 0 = lower lobe, 1 = upper lobe
                val ySigned = (cf - 0.5f) * 2f
                // Forked swallowtail indentation in center
                val notch = 1f - 0.42f * (1f - ySigned * ySigned) * rf
                val x = baseX * notch - 0.04f * rf * (1f - ySigned * ySigned)
                val y = ySigned * spreadY
                val z = 0.015f * sin(cf * PI.toFloat()) * (1f - rf * 0.5f)
                addVertex(x, y, z, -0.25f, ySigned * 0.25f, 0.92f, cf, spineT, 3f)
            }
        }
        val tStride = tailCols + 1
        for (r in 0 until tailRows) {
            for (c in 0 until tailCols) {
                val v0 = (tailBase + r * tStride + c).toShort()
                val v1 = (tailBase + (r + 1) * tStride + c).toShort()
                indexList.add(v0)
                indexList.add(v1)
                indexList.add((v0 + 1).toShort())
                indexList.add((v0 + 1).toShort())
                indexList.add(v1)
                indexList.add((v1 + 1).toShort())
            }
        }

        val vertices = FloatArray(vertexList.size) { vertexList[it] }
        val indices = ShortArray(indexList.size) { indexList[it] }
        return buildMesh(vertices, indices, intArrayOf(3, 3, 3))
    }

    private fun buildMesh(
        vertices: FloatArray,
        indices: ShortArray,
        attribSizes: IntArray
    ): GlMesh {
        val vaoArr = IntArray(1)
        val bufArr = IntArray(2)
        GLES30.glGenVertexArrays(1, vaoArr, 0)
        GLES30.glGenBuffers(2, bufArr, 0)

        val vao = vaoArr[0]
        val vbo = bufArr[0]
        val ibo = bufArr[1]

        val vertexBuffer: FloatBuffer = ByteBuffer
            .allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertices)
        vertexBuffer.position(0)

        val indexBuffer: ShortBuffer = ByteBuffer
            .allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(indices)
        indexBuffer.position(0)

        GLES30.glBindVertexArray(vao)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            vertices.size * 4,
            vertexBuffer,
            GLES30.GL_STATIC_DRAW
        )

        val totalFloats = attribSizes.sum()
        val byteStride = totalFloats * 4
        var offsetBytes = 0
        for (i in attribSizes.indices) {
            GLES30.glEnableVertexAttribArray(i)
            GLES30.glVertexAttribPointer(
                i,
                attribSizes[i],
                GLES30.GL_FLOAT,
                false,
                byteStride,
                offsetBytes
            )
            offsetBytes += attribSizes[i] * 4
        }

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ibo)
        GLES30.glBufferData(
            GLES30.GL_ELEMENT_ARRAY_BUFFER,
            indices.size * 2,
            indexBuffer,
            GLES30.GL_STATIC_DRAW
        )

        GLES30.glBindVertexArray(0)
        return GlMesh(vao, vbo, ibo, indices.size)
    }
}

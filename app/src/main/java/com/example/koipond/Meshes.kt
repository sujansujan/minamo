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

/**
 * Holds VAO/VBO/IBO handles for zero-allocation draw calls.
 */
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

    fun drawInstanced(instanceCount: Int) {
        GLES30.glBindVertexArray(vao)
        GLES30.glDrawElementsInstanced(
            GLES30.GL_TRIANGLES,
            indexCount,
            GLES30.GL_UNSIGNED_SHORT,
            0,
            instanceCount
        )
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
     * Unit quad [-1, 1] for foliage, lily pads, lotus flowers, and particle billboards.
     * Vertex layout: [posX, posY, u, v] (4 floats)
     */
    fun createUnitQuad(): GlMesh = createFullscreenQuad()

    /**
     * Subdivided ribbon mesh for swaying underwater vallisneria blades.
     * u in [0, 1] across blade width, v in [0, 1] from root to tip.
     * Vertex layout: [localX, localY, u, v]
     */
    fun createVallisneriaRibbon(segmentsY: Int = 20): GlMesh {
        val verts = FloatArray((segmentsY + 1) * 2 * 4)
        var vIdx = 0
        for (i in 0..segmentsY) {
            val v = i.toFloat() / segmentsY.toFloat()
            val halfWidth = (1f - v.pow(1.6f) * 0.82f).coerceAtLeast(0.12f)
            // Left edge
            verts[vIdx++] = -halfWidth
            verts[vIdx++] = v
            verts[vIdx++] = 0f
            verts[vIdx++] = v
            // Right edge
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
     * Articulated 3D top-down Koi / Minnow body + pectoral, ventral, dorsal, and caudal fins.
     * Spine runs along Y from +0.5 (snout, spineT=0) to -0.65 (tail tip, spineT=1).
     * Vertex layout:
     *   aPos:    vec3 (x, y, z)
     *   aNormal: vec3 (nx, ny, nz)
     *   aUvPart: vec3 (u, spineT, partId)
     *     where partId: 0.0 = main body, 1.0 = pectoral fin, 2.0 = dorsal fin, 3.0 = caudal tail fin
     */
    fun createFishMesh(spineSegments: Int = 32, radialSegments: Int = 14): GlMesh {
        val vertexList = ArrayList<Float>(4096)
        val indexList = ArrayList<Short>(8192)

        fun addVertex(
            x: Float, y: Float, z: Float,
            nx: Float, ny: Float, nz: Float,
            u: Float, spineT: Float, partId: Float
        ): Short {
            val idx = (vertexList.size / 9).toShort()
            val len = sqrt(nx * nx + ny * ny + nz * nz).coerceAtLeast(1e-4f)
            vertexList.add(x)
            vertexList.add(y)
            vertexList.add(z)
            vertexList.add(nx / len)
            vertexList.add(ny / len)
            vertexList.add(nz / len)
            vertexList.add(u)
            vertexList.add(spineT)
            vertexList.add(partId)
            return idx
        }

        // 1. Main fusiform koi body (upper + lower dome so top-down normal shading is smooth 3D)
        val stride = radialSegments + 1
        for (i in 0..spineSegments) {
            val t = i.toFloat() / spineSegments.toFloat() // 0 = nose, 1 = tail base
            val spineY = 0.50f - t * 0.92f

            // Teardrop fusiform width profile: widest around t = 0.24 (shoulders)
            val widthProfile = if (t < 0.24f) {
                val nt = t / 0.24f
                0.22f * sqrt(nt.coerceAtLeast(0f)) * (0.75f + 0.25f * nt)
            } else {
                val nt = (t - 0.24f) / 0.76f
                0.22f * (1f - nt).pow(1.45f) + 0.024f * (1f - nt)
            }
            val heightProfile = widthProfile * 0.85f

            for (j in 0..radialSegments) {
                val rFrac = j.toFloat() / radialSegments.toFloat() // 0 = left flank, 0.5 = dorsal crest, 1 = right flank
                val angle = (1f - rFrac) * PI.toFloat() // PI to 0 across upper dome
                val cx = cos(angle)
                val sz = sin(angle)

                val x = cx * widthProfile
                val z = sz * heightProfile
                val nx = cx * 0.9f
                val ny = (0.22f - t) * 0.35f
                val nz = sz.coerceAtLeast(0.15f)

                addVertex(x, spineY, z, nx, ny, nz, rFrac, t, 0f)
            }
        }

        for (i in 0 until spineSegments) {
            for (j in 0 until radialSegments) {
                val r0 = (i * stride + j).toShort()
                val r1 = ((i + 1) * stride + j).toShort()
                indexList.add(r0)
                indexList.add((r0 + 1).toShort())
                indexList.add(r1)

                indexList.add(r1)
                indexList.add((r0 + 1).toShort())
                indexList.add((r1 + 1).toShort())
            }
        }

        // 2. Pectoral fins (left and right fans at t = 0.22..0.38)
        fun addFinFan(side: Float, attachY: Float, attachX: Float, spineT: Float, radiusX: Float, radiusY: Float, partId: Float) {
            val fanSegs = 8
            val rootIdx = addVertex(
                attachX * side, attachY, 0.02f,
                0f, 0f, 1f,
                0f, spineT, partId
            )
            val ringStart = (vertexList.size / 9)
            for (k in 0..fanSegs) {
                val f = k.toFloat() / fanSegs.toFloat()
                val ang = (-0.25f + f * 1.35f)
                val fx = attachX * side + side * sin(ang) * radiusX
                val fy = attachY - cos(ang) * radiusY
                addVertex(
                    fx, fy, 0.01f,
                    side * 0.25f, -0.15f, 0.95f,
                    1f, (spineT + f * 0.12f).coerceAtMost(1f), partId
                )
            }
            for (k in 0 until fanSegs) {
                val v0 = (ringStart + k).toShort()
                val v1 = (ringStart + k + 1).toShort()
                if (side > 0f) {
                    indexList.add(rootIdx)
                    indexList.add(v0)
                    indexList.add(v1)
                } else {
                    indexList.add(rootIdx)
                    indexList.add(v1)
                    indexList.add(v0)
                }
            }
        }

        // Pectoral fins
        addFinFan(-1f, 0.29f, 0.16f, 0.24f, 0.23f, 0.22f, 1f)
        addFinFan(+1f, 0.29f, 0.16f, 0.24f, 0.23f, 0.22f, 1f)

        // Ventral (pelvic) smaller fins
        addFinFan(-1f, 0.02f, 0.11f, 0.52f, 0.13f, 0.14f, 1f)
        addFinFan(+1f, 0.02f, 0.11f, 0.52f, 0.13f, 0.14f, 1f)

        // 3. Flowing Caudal (Tail) Fin (t = 0.88 .. 1.25)
        val tailRows = 10
        val tailCols = 8
        val tailBaseVertex = vertexList.size / 9
        for (r in 0..tailRows) {
            val rf = r.toFloat() / tailRows.toFloat()
            val spineT = 0.86f + rf * 0.36f
            val y = -0.34f - rf * 0.34f
            val spread = 0.03f + rf * 0.24f
            for (c in 0..tailCols) {
                val cf = c.toFloat() / tailCols.toFloat() // 0..1 across left-to-right lobes
                val uSigned = (cf - 0.5f) * 2f
                // Bilobed swallowtail notch in the center
                val lobeNotch = 1f - 0.38f * (1f - uSigned * uSigned) * rf
                val x = uSigned * spread
                val yNotched = y * lobeNotch
                val z = 0.03f * (1f - rf * 0.7f) + 0.015f * cos(uSigned * PI.toFloat())
                addVertex(x, yNotched, z, uSigned * 0.3f, -0.2f, 0.92f, cf, spineT, 3f)
            }
        }
        val tStride = tailCols + 1
        for (r in 0 until tailRows) {
            for (c in 0 until tailCols) {
                val v0 = (tailBaseVertex + r * tStride + c).toShort()
                val v1 = (tailBaseVertex + (r + 1) * tStride + c).toShort()
                indexList.add(v0)
                indexList.add((v0 + 1).toShort())
                indexList.add(v1)
                indexList.add(v1)
                indexList.add((v0 + 1).toShort())
                indexList.add((v1 + 1).toShort())
            }
        }

        // 4. Dorsal fin ridge (t = 0.34 .. 0.74)
        val dorsalSegs = 10
        val dorsalBase = vertexList.size / 9
        for (i in 0..dorsalSegs) {
            val df = i.toFloat() / dorsalSegs.toFloat()
            val spineT = 0.34f + df * 0.40f
            val y = 0.50f - spineT * 0.92f
            val arch = sin(df * PI.toFloat())
            // Root on spine
            addVertex(0f, y, 0.16f * (1f - df * 0.45f), 0f, 0f, 1f, 0f, spineT, 2f)
            // Elevated trailing ridge
            addVertex(
                0.025f * sin(df * 2f),
                y - 0.04f,
                0.24f * (1f - df * 0.35f) + 0.06f * arch,
                0.4f, 0f, 0.9f,
                1f, spineT + 0.05f, 2f
            )
        }
        for (i in 0 until dorsalSegs) {
            val v0 = (dorsalBase + i * 2).toShort()
            val v1 = (dorsalBase + (i + 1) * 2).toShort()
            indexList.add(v0)
            indexList.add((v0 + 1).toShort())
            indexList.add(v1)
            indexList.add(v1)
            indexList.add((v0 + 1).toShort())
            indexList.add((v1 + 1).toShort())
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

package pw.janyo.whatanime.api

import pw.janyo.whatanime.model.request.SignatureRequest
import pw.janyo.whatanime.model.request.TestRequest
import pw.janyo.whatanime.model.response.SignatureResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import vip.mystery0.tools.model.Response

interface ServerApi {
	@POST("/api/rest/v1/what/anime/test/op")
	suspend fun testOp(@Body test: TestRequest): Response<*>

	@PUT("/api/rest/v1/what/anime/search/signature")
	suspend fun signature(signature: SignatureRequest): Response<SignatureResponse>
}
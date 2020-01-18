package pw.janyo.whatanime.api

import okhttp3.MultipartBody
import pw.janyo.whatanime.model.request.SignatureRequest
import pw.janyo.whatanime.model.request.TestRequest
import pw.janyo.whatanime.model.response.SignatureResponse
import pw.janyo.whatanime.model.response.UploadFileResponse
import retrofit2.http.*
import vip.mystery0.tools.model.Response

interface ServerApi {
	@POST("/api/rest/v1/what/anime/test/op")
	suspend fun testOp(@Body test: TestRequest): Response<*>

	@PUT("/api/rest/v1/what/anime/search/signature")
	suspend fun signature(@Body signature: SignatureRequest): Response<SignatureResponse>

	@POST
	@Multipart
	suspend fun uploadFile(@Url url: String,
						   @Part keyBody: MultipartBody.Part,
						   @Part tokenBody: MultipartBody.Part,
						   @Part file: MultipartBody.Part): Response<UploadFileResponse>
}
package pw.janyo.whatanime.api

import okhttp3.RequestBody
import pw.janyo.whatanime.model.request.SignatureRequest
import pw.janyo.whatanime.model.request.TestRequest
import pw.janyo.whatanime.model.response.SignatureResponse
import pw.janyo.whatanime.model.response.StatisticsResponse
import pw.janyo.whatanime.model.response.UploadFileResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Url

interface ServerApi {
	@POST("/api/rest/v2/what/anime/test/statistics")
	suspend fun testStatistics(@Body test: TestRequest): StatisticsResponse

	@PUT("/api/rest/v2/what/anime/search/signature")
	suspend fun signature(@Body signature: SignatureRequest): SignatureResponse

	@POST
	suspend fun uploadFile(@Url url: String,
						   @Body body: RequestBody): UploadFileResponse
}
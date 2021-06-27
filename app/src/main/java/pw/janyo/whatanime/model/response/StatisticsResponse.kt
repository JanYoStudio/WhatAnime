package pw.janyo.whatanime.model.response

data class StatisticsResponse(
    var inBlackList: Boolean,
    var useCloudCompress: Boolean?,
    var appCenterSecret: String,
)
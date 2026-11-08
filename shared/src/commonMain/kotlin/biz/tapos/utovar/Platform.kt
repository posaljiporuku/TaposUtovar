package biz.tapos.utovar

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
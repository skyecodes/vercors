package app.vercors.launcher.demo

enum class DemoScreen(val attribute: String) {
    Home("home"),
    Instance("instance"),
    Content("content");

    companion object {
        fun fromAttribute(value: String?): DemoScreen? = entries.firstOrNull { it.attribute == value }
    }
}

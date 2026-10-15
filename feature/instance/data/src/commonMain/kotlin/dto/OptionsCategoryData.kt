package app.vercors.feature.instance.data.dto

import app.vercors.feature.instance.domain.model.options.OptionsCategory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class OptionsCategoryData {
    @SerialName("keybinds")
    Keybinds,

    @SerialName("video")
    Video,

    @SerialName("audio")
    Audio,

    @SerialName("controls")
    Controls,

    @SerialName("chat")
    Chat,

    @SerialName("accessibility")
    Accessibility,

    @SerialName("language")
    Language,

    @SerialName("skin")
    Skin,

    @SerialName("resource_packs")
    ResourcePacks,

    @SerialName("other")
    Other,

    @SerialName("server_list")
    ServerList,

    @SerialName("hotbars")
    Hotbars,
}

fun OptionsCategoryData.toDomain(): OptionsCategory = when (this) {
    OptionsCategoryData.Keybinds -> OptionsCategory.Keybinds
    OptionsCategoryData.Video -> OptionsCategory.Video
    OptionsCategoryData.Audio -> OptionsCategory.Audio
    OptionsCategoryData.Controls -> OptionsCategory.Controls
    OptionsCategoryData.Chat -> OptionsCategory.Chat
    OptionsCategoryData.Accessibility -> OptionsCategory.Accessibility
    OptionsCategoryData.Language -> OptionsCategory.Language
    OptionsCategoryData.Skin -> OptionsCategory.Skin
    OptionsCategoryData.ResourcePacks -> OptionsCategory.ResourcePacks
    OptionsCategoryData.Other -> OptionsCategory.Other
    OptionsCategoryData.ServerList -> OptionsCategory.ServerList
    OptionsCategoryData.Hotbars -> OptionsCategory.Hotbars
}

fun OptionsCategory.toData(): OptionsCategoryData = when (this) {
    OptionsCategory.Keybinds -> OptionsCategoryData.Keybinds
    OptionsCategory.Video -> OptionsCategoryData.Video
    OptionsCategory.Audio -> OptionsCategoryData.Audio
    OptionsCategory.Controls -> OptionsCategoryData.Controls
    OptionsCategory.Chat -> OptionsCategoryData.Chat
    OptionsCategory.Accessibility -> OptionsCategoryData.Accessibility
    OptionsCategory.Language -> OptionsCategoryData.Language
    OptionsCategory.Skin -> OptionsCategoryData.Skin
    OptionsCategory.ResourcePacks -> OptionsCategoryData.ResourcePacks
    OptionsCategory.Other -> OptionsCategoryData.Other
    OptionsCategory.ServerList -> OptionsCategoryData.ServerList
    OptionsCategory.Hotbars -> OptionsCategoryData.Hotbars
}

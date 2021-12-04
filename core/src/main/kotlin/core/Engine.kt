package com.homm3.livewallpaper.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.AssetLoaderParameters
import com.homm3.livewallpaper.parser.formats.H3m
import core.layers.H3mLayersGroup
import core.screens.GameScreen
import core.screens.LoadingScreen
import core.screens.SelectAssetsScreen
import ktx.app.KtxGame

open class Engine : KtxGame<Screen>(null, false) {
    lateinit var assets: Assets

    open fun onSettingsButtonClick() {}

    private val mapsList: MutableList<H3m> = mutableListOf()
    private val mapsLayers: MutableList<H3mLayersGroup> = mutableListOf()

    override fun create() {
        assets = Assets()
        assets.loadUiAssets()

        addScreen(LoadingScreen(assets))
        addScreen(SelectAssetsScreen(assets, ::onSettingsButtonClick))
        addScreen(GameScreen())

        loadAndStart()
        loadMaps()
    }

    private fun loadAndStart() {
        setScreen<LoadingScreen>()

        if (assets.isGameAssetsAvailable()) {
            assets.loadGameAssets()
            setScreen<GameScreen>()
        } else {
            setScreen<SelectAssetsScreen>()
        }
    }

    private fun loadMaps() {
        Gdx.files
            .internal("maps")
            .list(".h3m")
            .filter { it.length() > 0L }
            .sortedBy { it.length() }
            .forEach { fileHandle ->
                Gdx.app.log("h3mLayer", "start loading ${fileHandle.file()}")
                assets
                    .manager
                    .load(
                        fileHandle.file().toString(),
                        H3m::class.java,
                        H3mLoaderParams().apply {
                            loadedCallback =
                                AssetLoaderParameters.LoadedCallback { aManager, fileName, _ ->
                                    Gdx.app.log("h3mLayer", "CB DONE ${fileHandle.file()}")

                                    mapsList.add(aManager.get(fileName))
                                }
                        })
            }

        assets.manager.finishLoading()

        mapsList.forEach {
            getScreen<GameScreen>().addMap(
                H3mLayersGroup(assets, it)
            )
        }
    }

    override fun resume() {
        super.resume()

        if (assets.isGameAssetsLoaded()) {
            setScreen<GameScreen>()
        } else {
            loadAndStart()
        }
    }

    override fun render() {
        assets.manager.update()

        super.render()
    }
}
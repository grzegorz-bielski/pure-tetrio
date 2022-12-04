import { defineConfig } from 'vitest/config'
import * as fs from 'fs'

import { pureCssPlugin } from "./vite/plugin";

const scalaVersion = fs.readFileSync("./scalaVersion.txt", { encoding: "utf-8" })
const feModuleName = "frontend"
const getAppPath = suffix => `./target/scala-${scalaVersion}/${feModuleName}-${suffix}`

export default defineConfig({
    test: {
        includeSource: ['vite/**/*.ts'],
    },
    plugins: [
        pureCssPlugin(),
    ],
    base: "/pure-tetrio/", // GHP specific
    build: {
        // TODO: scala-js module splitting is borked?
        // rollupOptions: {
        //     output: {
        //         manualChunks(id) {
        //             // getting `Uncaught TypeError: (intermediate value).initClass is not a function`

        //             // if (id.includes("internal")) {
        //             //     return "vendor"
        //             // }

        //             // if (id.includes("pureframes.tetrio")) {
        //             //     return "app"
        //             // }
        //         }
        //     }
        // }
    },
    resolve: {
        alias: [
            {
                find: "@app",
                replacement: getAppPath(
                    process.env.NODE_ENV == "production" ? "opt" : "fastopt"
                )
            }
        ]
    }
})
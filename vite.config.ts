import { defineConfig } from 'vite'
import * as fs from 'node:fs'
import * as url from 'node:url'

const scalaVersion = fs.readFileSync("./scalaVersion.txt", { encoding: "utf-8" })
const feModuleName = "frontend"
const getAppPath = (suffix: string) => `./target/scala-${scalaVersion}/${feModuleName}-${suffix}`

export default defineConfig({
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
                find: "@styles",
                replacement: url.fileURLToPath(new url.URL(`${feModuleName}/styles`, import.meta.url))
              },
            {
                find: "@app",
                replacement: getAppPath(
                    process.env.NODE_ENV == "production" ? "opt" : "fastopt"
                )
            }
        ]
    }
})

import { defineConfig } from "vite";
import fs from 'fs'

const scalaVersion = fs.readFileSync("./scalaVersion.txt", { encoding: "utf-8" })
const feModuleName = "frontend"
const getAppPath = suffix => `./target/scala-${scalaVersion}/${feModuleName}-${suffix}`

export default defineConfig({
    base: "pure-tetrio", // GHP specific
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
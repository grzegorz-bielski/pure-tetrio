import { defineConfig } from "vite";
import fs from 'fs'

const scalaVersion = fs.readFileSync("./scalaVersion.txt", { encoding: "utf-8" })
const projectname = "indigotetris"
const getAppPath = suffix => `./target/scala-${scalaVersion}/${projectname}-${suffix}`

export default defineConfig({
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
import { Plugin } from "vite";
import { createFilter } from "@rollup/pluginutils";

const startPragma = "/* PURE_CSS_START */"
const endPragma = "/* PURE_CSS_END */"

function replace(source: string): string {
    const start = source.indexOf(startPragma);

    if (start == -1) {
      return source
    }

    const end = source.indexOf(endPragma);

    // ignore partial patterns
    if (end == -1) {
        return source
    }

    return source.slice(0, start) + source.slice(end + endPragma.length)
  }

export function pureCssPlugin(): Plugin {
  const filter = createFilter(["**/*.js"]);

  return {
    name: "vite-scala-pure-css-plugin",
    enforce: "pre",
    apply: "build",

    // TODO: some chunks like unused css macro code are not processed
    // could it be because they are only used during `run` but still collected by the stylesheet context?

    transform(source, id) {
      if (!filter(id)) return;
      return replace(source);
    },
  };
}

if (import.meta.vitest) {
    const { it, expect } = import.meta.vitest
    
    it('replace before end', () => {
       const result = replace(`
       abvx
        ${startPragma}
        background: tomato;
        color: black;
        ${endPragma}d
        xdd
        `
       )

       expect(result.replace(/\s/g,'')).toBe('abvxdxdd')
    })

    it('replace to the end', () => {
        const result = replace(`
        abvx
         ${startPragma}
         background: tomato;
         color: black;
         ${endPragma}`
        )
 
        expect(result.replace(/\s/g,'')).toBe('abvx')
     })

     it('replaces nothing when no pragma is found', () => {
        const content = `
        abvx
         background: tomato;
         color: black;`

        const result = replace(content)

        expect(result).toBe(content)
     })
  }

package com.zp4rker.pluginprofiler

import org.fusesource.jansi.AnsiConsole
import org.fusesource.jansi.AnsiRenderer
import org.json.JSONObject
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.net.URLClassLoader

object PluginProfiler {

    @JvmStatic
    fun main(args: Array<String>) {
        AnsiConsole.systemInstall()

        val file = File(args[0])

        val cl = URLClassLoader(arrayOf(file.toURI().toURL()))
        val resource = cl.findResource("plugin.yml")

        val obj = Yaml().load<Map<String, Any>>(resource.readText())

        printOverview(obj)

        val extras = mutableSetOf<String>()
        for (rawArg in args) {
            var arg = ""

            if (rawArg.startsWith("--")) arg = rawArg.drop(2).toLowerCase()

            if (arg.isNotEmpty()) {
                when (arg) {
                    "command", "commands", "c" -> extras.add("c")
                    "permisison", "permissions", "p" -> extras.add("p")
                    "json" -> extras.add("j")
                }
            }
        }
        extras.forEach { if (it == "c") printCommands(obj) else if (it == "p") printPermissions(obj) else printJSON(obj) }
    }

    private fun printOverview(obj: Map<String, Any>) {
        val output = mutableSetOf("@|blue,bold OVERVIEW|@", "@|yellow Name:|@ ${obj["name"]}", "@|yellow Version:|@ ${obj["version"]}")
        arrayOf("author", "authors", "description").forEach { obj[it]?.apply { output.add("@|yellow ${it.capitalize()}:|@ $this") } }

        println(AnsiRenderer.render(output.joinToString("\n")))
    }

    private fun printCommands(obj: Map<String, Any>) {
        val output = mutableSetOf("", "@|blue,bold COMMANDS|@")

        val commandsObj = obj["commands"] as Map<*, *>
        for (command in commandsObj.keys) {
            output.add("@|yellow $command:|@".toLowerCase())
            val commandObj = commandsObj[command] as Map<*, *>
            arrayOf("aliases", "usage", "description", "permission").forEach { commandObj[it]?.apply { output.add("  @|bold ${it.capitalize()}:|@ $this") } }
        }

        println(AnsiRenderer.render(output.joinToString("\n")))
    }

    private fun printPermissions(obj: Map<String, Any>) {
        val output = mutableSetOf("", "@|blue,bold PERMISSIONS|@")

        val permissionsObj = obj["permissions"] as Map<*, *>
        for (permission in permissionsObj.keys) {
            output.add("@|yellow $permission:|@".toLowerCase())
            val permissionObj = permissionsObj[permission] as Map<*, *>
            arrayOf("default", "description").forEach { permissionObj[it]?.apply { output.add("  @|bold ${it.capitalize()}:|@ $this") } }
        }

        println(AnsiRenderer.render(output.joinToString("\n")))
    }

    private fun printJSON(obj: Map<String, Any>) {
        val out = linkedMapOf<String, Any>()

        for (entry in obj.entries) {
            if (entry.value !is Map<*, *>) out[entry.key] = entry.value
            else {
                out[entry.key] = mapToObject(entry.value as LinkedHashMap<*, *>)
            }
        }

        println("\n${JSONObject(out).toString(2)}\n")
    }

    private fun mapToObject(obj: LinkedHashMap<*, *>): Any = if (obj.values.all { it is Map<*, *> }) obj.entries.map { mapOf(it.key to it.value) } else obj

}
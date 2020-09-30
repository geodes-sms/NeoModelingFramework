package geodes.sms.nmf.loader

import geodes.sms.nmf.loader.emf2neo4j.EmfModelLoader
import geodes.sms.nmf.neo4j.io.GraphBatchWriter
import org.apache.commons.cli.*

fun main(args: Array<String>) {
    val options = Options()
    Option("h", true, "Database host address with port")
    options.addOption(Option.builder("h")
        .longOpt("host")
        .argName("HOST:PORT")
        .desc("Database host address with port used to create bolt connection. Example: -h 127.0.0.1:7687")
        .hasArg()
        .build())

    options.addOption(Option.builder("u")
        .longOpt("user")
        .desc("Database auth: username")
        .hasArg()
        .required()
        .build())

    options.addOption(Option.builder("p")
        .longOpt("password")
        .desc("Database auth: password")
        .hasArg()
        .required()
        .build())

    options.addOption(Option.builder("m")
        .longOpt("model")
        .argName("PATH")
        .desc("path to model file to be loaded")
        .hasArg()
        .required()
        .build())

    try {
        val parser: CommandLineParser = DefaultParser()
        val cmd = parser.parse(options, args)

        val host = cmd.getOptionValue("host", "127.0.0.1:7687")
        val user = cmd.getOptionValue("user", "neo4j")
        val pwd = cmd.getOptionValue("password")
        val model = cmd.getOptionValue("model")

        GraphBatchWriter("bolt://$host", user, pwd).use { writer ->
            val (nodes, refs) = EmfModelLoader.load(model, writer)
            println("Nodes loaded: $nodes")
            println("Refs loaded: $refs")
        }
    } catch (e: ParseException) {
        val formatter = HelpFormatter()
        formatter.printHelp("Loader", options)
    } catch (e: Exception) {
        println("Cannot load the model")
        e.printStackTrace()
    }
}
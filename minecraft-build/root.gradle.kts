plugins {
    base
    id("gg.essential.multi-version.root")
}

base.archivesName = "kairos-ui"
version = "0.2.0"

preprocess {
    val forge12001 = createNode("1.20.1-forge", 12001, "srg")
    val forge11202 = createNode("1.12.2-forge", 11202, "srg")
    forge12001.link(forge11202)
}

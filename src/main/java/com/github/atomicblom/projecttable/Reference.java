package com.github.atomicblom.projecttable;


import net.minecraft.resources.ResourceLocation;

public class Reference {
    public static class Block {
        public static final ResourceLocation PROJECT_TABLE = resource("project_table");


        private Block() {}
    }

    public static class Container {
        public static final ResourceLocation PROJECT_TABLE = resource("project_table_container");


        private Container() {}
    }


    private static ResourceLocation resource(String name) {
        return new ResourceLocation(ProjectTableMod.MODID, name);
    }

    private Reference() {}
}

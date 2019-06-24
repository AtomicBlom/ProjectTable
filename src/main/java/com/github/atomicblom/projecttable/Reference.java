package com.github.atomicblom.projecttable;

import net.minecraft.util.ResourceLocation;

public class Reference {
    public static class Block {
        public static final ResourceLocation PROJECT_TABLE = resource("project_table");


        private Block() {}
    }


    private static ResourceLocation resource(String name) {
        return new ResourceLocation(ProjectTableMod.MODID, name);
    }

    private Reference() {}
}

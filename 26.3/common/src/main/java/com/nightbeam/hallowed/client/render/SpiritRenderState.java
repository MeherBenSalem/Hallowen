package com.nightbeam.hallowed.client.render;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class SpiritRenderState extends LivingEntityRenderState {
    public boolean charging;
    /** 0 while rising out of the ground, 1 once fully risen. */
    public float risen = 1;
    public float flicker = 1;
}

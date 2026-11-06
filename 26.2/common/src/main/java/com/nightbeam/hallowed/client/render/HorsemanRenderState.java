package com.nightbeam.hallowed.client.render;

import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

public class HorsemanRenderState extends HumanoidRenderState {
    public final EquineRenderState steed = new EquineRenderState();
    public boolean mounted;
    public boolean headless;
    public byte action;
    public float actionAge;
    /** How far the Horseman still has to rise out of the ground (blocks). */
    public float sink;
    public int overlay;
}

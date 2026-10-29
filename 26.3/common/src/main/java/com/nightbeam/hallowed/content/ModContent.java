package com.nightbeam.hallowed.content;

import com.nightbeam.hallowed.Hallowed;
import com.nightbeam.hallowed.block.CarvedFaceBlock;
import com.nightbeam.hallowed.block.CursedPumpkinBlock;
import com.nightbeam.hallowed.block.HallowedCacheBlock;
import com.nightbeam.hallowed.effect.HallowedEffect;
import com.nightbeam.hallowed.entity.HollowHorseman;
import com.nightbeam.hallowed.entity.PumpkinBomb;
import com.nightbeam.hallowed.entity.Spirit;
import com.nightbeam.hallowed.entity.Wisp;
import com.nightbeam.hallowed.item.CandyItem;
import com.nightbeam.hallowed.item.CarvedFaceItem;
import com.nightbeam.hallowed.item.HorsemanHeadItem;
import com.nightbeam.hallowed.item.ReaperScytheItem;
import com.nightbeam.hallowed.item.TreatPailItem;
import com.nightbeam.hallowed.platform.Registrar;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/** Everything the mod registers. */
public final class ModContent {
    public static final ResourceKey<PoiType> LANTERN_WARD_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, Hallowed.id("lantern_ward"));
    public static final ResourceKey<JukeboxSong> WITCHING_HOUR_SONG = ResourceKey.create(Registries.JUKEBOX_SONG, Hallowed.id("witching_hour"));

    // Blocks
    public static Supplier<Block> CARVED_PUMPKIN;
    public static Supplier<Block> JACK_O_LANTERN;
    public static Supplier<Block> SOUL_JACK_O_LANTERN;
    public static Supplier<Block> HALLOWED_CACHE;
    public static Supplier<Block> CURSED_PUMPKIN;

    // Items
    public static final Map<CandyType, Supplier<Item>> CANDY = new EnumMap<>(CandyType.class);
    public static Supplier<Item> CARVED_PUMPKIN_ITEM;
    public static Supplier<Item> JACK_O_LANTERN_ITEM;
    public static Supplier<Item> SOUL_JACK_O_LANTERN_ITEM;
    public static Supplier<Item> HALLOWED_CACHE_ITEM;
    public static Supplier<Item> CURSED_PUMPKIN_ITEM;
    public static Supplier<Item> ECTOPLASM;
    public static Supplier<Item> TREAT_PAIL;
    public static Supplier<Item> WITCH_HAT;
    public static Supplier<Item> HORSEMAN_HEAD;
    public static Supplier<Item> REAPER_SCYTHE;
    public static Supplier<Item> MUSIC_DISC_WITCHING_HOUR;
    public static Supplier<Item> SPIRIT_SPAWN_EGG;
    public static Supplier<Item> WISP_SPAWN_EGG;
    public static Supplier<Item> HORSEMAN_SPAWN_EGG;

    // Entities
    public static Supplier<EntityType<Wisp>> WISP;
    public static Supplier<EntityType<Spirit>> SPIRIT;
    public static Supplier<EntityType<HollowHorseman>> HOLLOW_HORSEMAN;
    public static Supplier<EntityType<PumpkinBomb>> PUMPKIN_BOMB;

    // Effects
    public static Supplier<MobEffect> SUGAR_RUSH;
    public static Supplier<MobEffect> SUGAR_CRASH;
    public static Supplier<MobEffect> SPOOKED;
    public static Supplier<MobEffect> SPIRIT_WARD;

    // Particles
    public static Supplier<SimpleParticleType> EMBER;
    public static Supplier<SimpleParticleType> AUTUMN_LEAF;
    public static Supplier<SimpleParticleType> WISP_FLAME;
    public static Supplier<SimpleParticleType> SOUL_MOTE;
    public static Supplier<SimpleParticleType> CANDY_SPARKLE;

    public static Supplier<PoiType> LANTERN_WARD;
    public static Supplier<CreativeModeTab> TAB;

    // Sounds
    public static Supplier<SoundEvent> WITCHING_BELL;
    public static Supplier<SoundEvent> AMBIENT_WHISPERS;
    public static Supplier<SoundEvent> AMBIENT_OWL;
    public static Supplier<SoundEvent> AMBIENT_CHIMES;
    public static Supplier<SoundEvent> AMBIENT_WIND;
    public static Supplier<SoundEvent> WISP_AMBIENT;
    public static Supplier<SoundEvent> WISP_CHIME;
    public static Supplier<SoundEvent> WISP_VANISH;
    public static Supplier<SoundEvent> CACHE_OPEN;
    public static Supplier<SoundEvent> CANDY_EAT;
    public static Supplier<SoundEvent> TREAT;
    public static Supplier<SoundEvent> TRICK;
    public static Supplier<SoundEvent> CARVE;
    public static Supplier<SoundEvent> SPIRIT_AMBIENT;
    public static Supplier<SoundEvent> SPIRIT_HURT;
    public static Supplier<SoundEvent> SPIRIT_DEATH;
    public static Supplier<SoundEvent> SPIRIT_CHARGE;
    public static Supplier<SoundEvent> HORSEMAN_LAUGH;
    public static Supplier<SoundEvent> HORSEMAN_SUMMON;
    public static Supplier<SoundEvent> HORSEMAN_CHARGE;
    public static Supplier<SoundEvent> HORSEMAN_HURT;
    public static Supplier<SoundEvent> HORSEMAN_DEATH;
    public static Supplier<SoundEvent> HORSEMAN_DISMOUNT;
    public static Supplier<SoundEvent> HORSEMAN_SWING;
    public static Supplier<SoundEvent> PUMPKIN_BOMB_THROW;
    public static Supplier<SoundEvent> PUMPKIN_BOMB_EXPLODE;
    public static Supplier<SoundEvent> SCYTHE_REAP;
    public static Supplier<SoundEvent> SPOOK;
    public static Supplier<SoundEvent> HEARTBEAT;
    public static Supplier<SoundEvent> CURSED_PULSE;
    public static Supplier<SoundEvent> MUSIC_WITCHING_HOUR;

    private ModContent() {
    }

    public static void register(Registrar r) {
        registerSounds(r);
        registerEffects(r);
        registerParticles(r);
        registerBlocks(r);
        registerEntities(r);
        registerItems(r);
        LANTERN_WARD = r.register(Registries.POINT_OF_INTEREST_TYPE, "lantern_ward", id -> new PoiType(wardStates(), 0, 1));
        TAB = r.register(Registries.CREATIVE_MODE_TAB, "hallowed", id -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.hallowed"))
                .icon(() -> faceStack(JACK_O_LANTERN_ITEM.get(), Face.GRIN))
                .displayItems((parameters, output) -> {
                    for (ItemStack stack : tabContents()) output.accept(stack);
                })
                .build());
    }

    // ----------------------------------------------------------------------------------------------------------------

    private static void registerBlocks(Registrar r) {
        CARVED_PUMPKIN = r.register(Registries.BLOCK, "carved_pumpkin", id -> new CarvedFaceBlock(CarvedFaceBlock.Kind.CARVED,
                pumpkinProps(id).isValidSpawn((s, l, p, t) -> true)));
        JACK_O_LANTERN = r.register(Registries.BLOCK, "jack_o_lantern", id -> new CarvedFaceBlock(CarvedFaceBlock.Kind.LIT,
                pumpkinProps(id).lightLevel(s -> 15).isValidSpawn((s, l, p, t) -> true)));
        SOUL_JACK_O_LANTERN = r.register(Registries.BLOCK, "soul_jack_o_lantern", id -> new CarvedFaceBlock(CarvedFaceBlock.Kind.SOUL,
                pumpkinProps(id).mapColor(MapColor.COLOR_LIGHT_BLUE).lightLevel(s -> 12).isValidSpawn((s, l, p, t) -> true)));
        HALLOWED_CACHE = r.register(Registries.BLOCK, "hallowed_cache", id -> new HallowedCacheBlock(BlockBehaviour.Properties.of()
                .setId(blockKey(id)).mapColor(MapColor.COLOR_BROWN).strength(2.0f, 6.0f).sound(SoundType.WOOD)
                .lightLevel(s -> 7).noOcclusion().pushReaction(PushReaction.IMMOVEABLE)));
        CURSED_PUMPKIN = r.register(Registries.BLOCK, "cursed_pumpkin", id -> new CursedPumpkinBlock(BlockBehaviour.Properties.of()
                .setId(blockKey(id)).mapColor(MapColor.COLOR_BLACK).strength(1.5f, 1200f).sound(SoundType.WOOD)
                .lightLevel(s -> 4 + s.getValue(CursedPumpkinBlock.STAGE) * 2).pushReaction(PushReaction.IMMOVEABLE)));
    }

    private static BlockBehaviour.Properties pumpkinProps(Identifier id) {
        return BlockBehaviour.Properties.of().setId(blockKey(id)).mapColor(MapColor.COLOR_ORANGE).strength(1.0f)
                .sound(SoundType.WOOD).pushReaction(PushReaction.POPPED);
    }

    private static void registerEntities(Registrar r) {
        WISP = r.register(Registries.ENTITY_TYPE, "will_o_wisp", id -> EntityType.Builder.<Wisp>of(Wisp::new, MobCategory.MISC)
                .sized(0.5f, 0.5f).eyeHeight(0.25f).clientTrackingRange(10).updateInterval(2).fireImmune().build(entityKey(id)));
        SPIRIT = r.register(Registries.ENTITY_TYPE, "spirit", id -> EntityType.Builder.<Spirit>of(Spirit::new, MobCategory.MONSTER)
                .sized(0.6f, 1.6f).eyeHeight(1.35f).clientTrackingRange(8).fireImmune().build(entityKey(id)));
        HOLLOW_HORSEMAN = r.register(Registries.ENTITY_TYPE, "hollow_horseman", id -> EntityType.Builder.<HollowHorseman>of(HollowHorseman::new, MobCategory.MONSTER)
                .sized(1.4f, 2.9f).eyeHeight(2.6f).clientTrackingRange(16).fireImmune().build(entityKey(id)));
        PUMPKIN_BOMB = r.register(Registries.ENTITY_TYPE, "pumpkin_bomb", id -> EntityType.Builder.<PumpkinBomb>of(PumpkinBomb::new, MobCategory.MISC)
                .sized(0.6f, 0.6f).clientTrackingRange(8).updateInterval(1).build(entityKey(id)));
    }

    private static void registerItems(Registrar r) {
        for (CandyType type : CandyType.values()) {
            CANDY.put(type, r.register(Registries.ITEM, type.id, id -> new CandyItem(type, item(id).rarity(type.rarity))));
        }
        CARVED_PUMPKIN_ITEM = r.register(Registries.ITEM, "carved_pumpkin", id -> new CarvedFaceItem(CARVED_PUMPKIN.get(), blockItem(id)
                .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).setSwappable(false)
                        .setCameraOverlay(Identifier.withDefaultNamespace("misc/pumpkinblur")).build())));
        JACK_O_LANTERN_ITEM = r.register(Registries.ITEM, "jack_o_lantern", id -> new CarvedFaceItem(JACK_O_LANTERN.get(), blockItem(id)
                .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).setSwappable(false).build())));
        SOUL_JACK_O_LANTERN_ITEM = r.register(Registries.ITEM, "soul_jack_o_lantern", id -> new CarvedFaceItem(SOUL_JACK_O_LANTERN.get(), blockItem(id)
                .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).setSwappable(false).build())));
        HALLOWED_CACHE_ITEM = r.register(Registries.ITEM, "hallowed_cache", id -> new BlockItem(HALLOWED_CACHE.get(), blockItem(id).rarity(Rarity.UNCOMMON)));
        CURSED_PUMPKIN_ITEM = r.register(Registries.ITEM, "cursed_pumpkin", id -> new BlockItem(CURSED_PUMPKIN.get(), blockItem(id).rarity(Rarity.RARE)));
        ECTOPLASM = r.register(Registries.ITEM, "ectoplasm", id -> new Item(item(id)));
        TREAT_PAIL = r.register(Registries.ITEM, "treat_pail", id -> new TreatPailItem(item(id).stacksTo(1)
                .component(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY)));
        WITCH_HAT = r.register(Registries.ITEM, "witch_hat", id -> new Item(item(id).stacksTo(1)
                .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).build())));
        HORSEMAN_HEAD = r.register(Registries.ITEM, "horseman_head", id -> new HorsemanHeadItem(item(id).stacksTo(1).rarity(Rarity.EPIC)
                .fireResistant().component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).build())));
        REAPER_SCYTHE = r.register(Registries.ITEM, "reaper_scythe", id -> new ReaperScytheItem(item(id).rarity(Rarity.EPIC).fireResistant()
                .sword(net.minecraft.world.item.ToolMaterial.NETHERITE, 4.0f, -2.8f)));
        MUSIC_DISC_WITCHING_HOUR = r.register(Registries.ITEM, "music_disc_witching_hour", id -> new Item(item(id).stacksTo(1)
                .rarity(Rarity.RARE).jukeboxPlayable(WITCHING_HOUR_SONG)));
        SPIRIT_SPAWN_EGG = r.register(Registries.ITEM, "spirit_spawn_egg", id -> new SpawnEggItem(item(id).spawnEgg(SPIRIT.get())));
        WISP_SPAWN_EGG = r.register(Registries.ITEM, "will_o_wisp_spawn_egg", id -> new SpawnEggItem(item(id).spawnEgg(WISP.get())));
        HORSEMAN_SPAWN_EGG = r.register(Registries.ITEM, "hollow_horseman_spawn_egg", id -> new SpawnEggItem(item(id).spawnEgg(HOLLOW_HORSEMAN.get())));
    }

    private static void registerEffects(Registrar r) {
        SUGAR_RUSH = r.register(Registries.MOB_EFFECT, "sugar_rush", id -> new HallowedEffect(MobEffectCategory.BENEFICIAL, 0xFF8A3D)
                .addAttributeModifier(Attributes.MOVEMENT_SPEED, Hallowed.id("sugar_rush_speed"), 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addAttributeModifier(Attributes.ATTACK_SPEED, Hallowed.id("sugar_rush_attack"), 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addAttributeModifier(Attributes.BLOCK_BREAK_SPEED, Hallowed.id("sugar_rush_mining"), 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        SUGAR_CRASH = r.register(Registries.MOB_EFFECT, "sugar_crash", id -> new HallowedEffect(MobEffectCategory.HARMFUL, 0x7A6A8C)
                .addAttributeModifier(Attributes.MOVEMENT_SPEED, Hallowed.id("sugar_crash_speed"), -0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addAttributeModifier(Attributes.ATTACK_SPEED, Hallowed.id("sugar_crash_attack"), -0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        SPOOKED = r.register(Registries.MOB_EFFECT, "spooked", id -> new HallowedEffect(MobEffectCategory.HARMFUL, 0x9B7BD4)
                .addAttributeModifier(Attributes.MOVEMENT_SPEED, Hallowed.id("spooked_speed"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        SPIRIT_WARD = r.register(Registries.MOB_EFFECT, "spirit_ward", id -> new HallowedEffect(MobEffectCategory.BENEFICIAL, 0x7FE3E0));
    }

    private static void registerParticles(Registrar r) {
        EMBER = particle(r, "ember");
        AUTUMN_LEAF = particle(r, "autumn_leaf");
        WISP_FLAME = particle(r, "wisp_flame");
        SOUL_MOTE = particle(r, "soul_mote");
        CANDY_SPARKLE = particle(r, "candy_sparkle");
    }

    private static void registerSounds(Registrar r) {
        WITCHING_BELL = sound(r, "event.witching_bell");
        AMBIENT_WHISPERS = sound(r, "ambient.whispers");
        AMBIENT_OWL = sound(r, "ambient.owl");
        AMBIENT_CHIMES = sound(r, "ambient.chimes");
        AMBIENT_WIND = sound(r, "ambient.wind");
        WISP_AMBIENT = sound(r, "entity.wisp.ambient");
        WISP_CHIME = sound(r, "entity.wisp.chime");
        WISP_VANISH = sound(r, "entity.wisp.vanish");
        CACHE_OPEN = sound(r, "block.hallowed_cache.open");
        CANDY_EAT = sound(r, "item.candy.eat");
        TREAT = sound(r, "event.treat");
        TRICK = sound(r, "event.trick");
        CARVE = sound(r, "block.pumpkin.carve");
        SPIRIT_AMBIENT = sound(r, "entity.spirit.ambient");
        SPIRIT_HURT = sound(r, "entity.spirit.hurt");
        SPIRIT_DEATH = sound(r, "entity.spirit.death");
        SPIRIT_CHARGE = sound(r, "entity.spirit.charge");
        HORSEMAN_LAUGH = sound(r, "entity.hollow_horseman.laugh");
        HORSEMAN_SUMMON = sound(r, "entity.hollow_horseman.summon");
        HORSEMAN_CHARGE = sound(r, "entity.hollow_horseman.charge");
        HORSEMAN_HURT = sound(r, "entity.hollow_horseman.hurt");
        HORSEMAN_DEATH = sound(r, "entity.hollow_horseman.death");
        HORSEMAN_DISMOUNT = sound(r, "entity.hollow_horseman.dismount");
        HORSEMAN_SWING = sound(r, "entity.hollow_horseman.swing");
        PUMPKIN_BOMB_THROW = sound(r, "entity.pumpkin_bomb.throw");
        PUMPKIN_BOMB_EXPLODE = sound(r, "entity.pumpkin_bomb.explode");
        SCYTHE_REAP = sound(r, "item.reaper_scythe.reap");
        SPOOK = sound(r, "event.spook");
        HEARTBEAT = sound(r, "effect.spooked.heartbeat");
        CURSED_PULSE = sound(r, "block.cursed_pumpkin.pulse");
        MUSIC_WITCHING_HOUR = sound(r, "music_disc.witching_hour");
    }

    // ----------------------------------------------------------------------------------------------------------------

    public static Item candy(CandyType type) {
        return CANDY.get(type).get();
    }

    public static Holder<MobEffect> holder(Supplier<MobEffect> effect) {
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect.get());
    }

    /** A carved-face block item showing {@code face}. */
    public static ItemStack faceStack(Item item, Face face) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(CarvedFaceBlock.FACE, face.ordinal()));
        return stack;
    }

    /** Every block state that wards off spirits: vanilla and Hallowed jack o'lanterns. */
    private static Set<BlockState> wardStates() {
        java.util.HashSet<BlockState> states = new java.util.HashSet<>(Blocks.JACK_O_LANTERN.getStateDefinition().getPossibleStates());
        states.addAll(JACK_O_LANTERN.get().getStateDefinition().getPossibleStates());
        states.addAll(SOUL_JACK_O_LANTERN.get().getStateDefinition().getPossibleStates());
        return Set.copyOf(states);
    }

    private static List<ItemStack> tabContents() {
        List<ItemStack> out = new java.util.ArrayList<>();
        out.add(new ItemStack(TREAT_PAIL.get()));
        for (CandyType type : CandyType.values()) out.add(new ItemStack(candy(type)));
        for (Face face : Face.values()) out.add(faceStack(CARVED_PUMPKIN_ITEM.get(), face));
        for (Face face : Face.values()) out.add(faceStack(JACK_O_LANTERN_ITEM.get(), face));
        for (Face face : Face.values()) out.add(faceStack(SOUL_JACK_O_LANTERN_ITEM.get(), face));
        out.add(new ItemStack(WITCH_HAT.get()));
        out.add(new ItemStack(ECTOPLASM.get()));
        out.add(new ItemStack(HALLOWED_CACHE_ITEM.get()));
        out.add(new ItemStack(CURSED_PUMPKIN_ITEM.get()));
        out.add(new ItemStack(HORSEMAN_HEAD.get()));
        out.add(new ItemStack(REAPER_SCYTHE.get()));
        out.add(new ItemStack(MUSIC_DISC_WITCHING_HOUR.get()));
        out.add(new ItemStack(SPIRIT_SPAWN_EGG.get()));
        out.add(new ItemStack(WISP_SPAWN_EGG.get()));
        out.add(new ItemStack(HORSEMAN_SPAWN_EGG.get()));
        return out;
    }

    private static Item.Properties item(Identifier id) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id));
    }

    private static Item.Properties blockItem(Identifier id) {
        return item(id).useBlockDescriptionPrefix();
    }

    private static ResourceKey<Block> blockKey(Identifier id) {
        return ResourceKey.create(Registries.BLOCK, id);
    }

    private static ResourceKey<EntityType<?>> entityKey(Identifier id) {
        return ResourceKey.create(Registries.ENTITY_TYPE, id);
    }

    private static Supplier<SoundEvent> sound(Registrar r, String path) {
        return r.register(Registries.SOUND_EVENT, path, SoundEvent::createVariableRangeEvent);
    }

    private static Supplier<SimpleParticleType> particle(Registrar r, String path) {
        return r.register(Registries.PARTICLE_TYPE, path, id -> new HallowedParticleType());
    }

    /** SimpleParticleType's constructor is not public on every loader; a subclass can always reach it. */
    private static final class HallowedParticleType extends SimpleParticleType {
        private HallowedParticleType() {
            super(false);
        }
    }

}

package rip.alpha.core.bukkit.npc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_7_R4.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.alpha.libraries.fake.impl.player.FakePlayerEntity;
import rip.alpha.libraries.hologram.Hologram;
import rip.alpha.libraries.hologram.HologramLine;
import rip.alpha.libraries.skin.MojangSkin;

import java.util.List;
import java.util.UUID;

public class NPC extends FakePlayerEntity {

    private static final double HOLOGRAM_OFFSET = 0.86D;

    private MojangSkin mojangSkin;

    private final ItemStack[] inventory;
    private final Hologram hologram;

    @Getter
    private boolean sitting;

    private final PacketPlayOutAttachEntity detachPacket;
    private PacketPlayOutSpawnEntity spawnArrow;
    private PacketPlayOutEntityDestroy destroyArrow;
    private int previousArrowId = -1;

    @Setter(AccessLevel.PROTECTED)
    private PacketPlayOutAttachEntity attachPacket;

    @Setter(AccessLevel.PROTECTED)
    private EntityArrow entityArrow;

    public NPC(NPCEntry entry) {
        super(entry.getId(), entry.getDisplayName(), entry.getLocation());
        this.inventory = entry.getInventory();
        this.hologram = new Hologram(entry.getLocation().clone().add(0, HOLOGRAM_OFFSET, 0));
        this.setMojangSkin(entry.getMojangSkin());
        entry.getHologramLines().forEach(this::addLine);
        this.sitting = entry.isSitting();

        if (this.sitting) {
            this.entityArrow = new EntityArrow(this.getEntityPlayer().getWorld());
            this.entityArrow.setLocation(this.getLocation().getX(), this.getLocation().getY(), this.getLocation().getZ(), 0, 90);
            this.spawnArrow = new PacketPlayOutSpawnEntity(this.entityArrow, 60);
            this.previousArrowId = this.entityArrow.getId();
            this.destroyArrow = new PacketPlayOutEntityDestroy(this.entityArrow.getId());
        }

        this.detachPacket = new PacketPlayOutAttachEntity();
        this.detachPacket.a = 0;
        this.detachPacket.b = this.getEntityId();
        this.detachPacket.c = -1;
    }

    @Override
    public boolean show(Player player) {
        super.show(player);

        if (this.isSitting()) {
            this.updateRide(player);
        }

        for (int i = 0; i < this.inventory.length; i++) {
            ItemStack itemStack = this.inventory[i];
            if (itemStack == null) {
                continue;
            }

            int id = this.getEntityPlayer().getId();
            net.minecraft.server.v1_7_R4.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
            PacketPlayOutEntityEquipment equipmentPacket = new PacketPlayOutEntityEquipment(id, i, nmsItemStack);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(equipmentPacket);
        }

        if (!this.hologram.isEmpty()) {
            this.hologram.setup(player);
        }

        return true;
    }

    @Override
    public boolean hide(Player player) {
        if (this.isSitting()) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(this.destroyArrow);
        }
        this.hologram.hide(player);
        return super.hide(player);
    }

    @Override
    public void teleport(Location location) {
        super.teleport(location);
        this.hologram.updateLocation(location.clone().add(0, HOLOGRAM_OFFSET, 0));

        if (this.sitting) {
            this.entityArrow.setLocation(getLocation().getX(), getLocation().getY(), getLocation().getZ(), 0, 90);
            this.spawnArrow.b = MathHelper.floor(this.entityArrow.locX * 32.0D);
            this.spawnArrow.c = MathHelper.floor(this.entityArrow.locY * 32.0D);
            this.spawnArrow.d = MathHelper.floor(this.entityArrow.locZ * 32.0D);
            PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport(this.entityArrow);
            for (UUID uuid : this.getCurrentlyViewing()) {
                Player player = Bukkit.getPlayer(uuid);

                if (player == null || !player.willBeOnline()) {
                    continue;
                }

                if (!player.getWorld().getName().equals(location.getWorld().getName())) {
                    continue;
                }

                this.updateRide(player);
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
            }
        }
    }

    protected void setSitting(boolean sitting) {
        this.sitting = sitting;

        if (!sitting) {
            this.entityArrow = null;
            this.showToAll();
            return;
        }

        if (this.entityArrow == null) {
            this.entityArrow = new EntityArrow(this.getEntityPlayer().getWorld());
            this.entityArrow.setLocation(this.getLocation().getX(), this.getLocation().getY(), this.getLocation().getZ(), 0, 90);
            this.spawnArrow = new PacketPlayOutSpawnEntity(this.entityArrow, 60);
            this.previousArrowId = this.entityArrow.getId();
            this.destroyArrow = new PacketPlayOutEntityDestroy(this.entityArrow.getId());
            this.attachPacket = new PacketPlayOutAttachEntity();
            this.attachPacket.a = 0;
            this.attachPacket.b = this.getEntityId();
            this.attachPacket.c = this.entityArrow.getId();
        }

        this.showToAll();
    }

    protected void setEquipment(int slot, ItemStack itemStack) {
        this.inventory[slot] = itemStack;
        this.showToAll();
    }

    protected void addLine(String line) {
        this.hologram.addLine(line);
        for (UUID uuid : this.getCurrentlyViewing()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                continue;
            }
            this.hologram.setup(player);
        }
    }

    protected void updateRideForViewers() {
        for (UUID uuid : this.getCurrentlyViewing()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.willBeOnline()) {
                continue;
            }
            this.updateRide(player);
        }
    }

    private void updateRide(Player player) {
        if (!this.sitting) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(this.detachPacket);
            return;
        }

        boolean removeLast = false;

        if (this.entityArrow == null) {
            this.entityArrow = new EntityArrow(this.getEntityPlayer().getWorld());
            this.entityArrow.setLocation(getLocation().getX(), getLocation().getY(), getLocation().getZ(), 0, 90);
            this.spawnArrow = new PacketPlayOutSpawnEntity(this.entityArrow, 60);
            this.destroyArrow = new PacketPlayOutEntityDestroy(this.entityArrow.getId());
            removeLast = (previousArrowId != -1);
        }

        if (this.attachPacket == null) {
            this.attachPacket = new PacketPlayOutAttachEntity();
            this.attachPacket.a = 0;
            this.attachPacket.b = this.getEntityId();
            this.attachPacket.c = this.entityArrow.getId();
        }

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(this.spawnArrow);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(this.attachPacket);

        if (removeLast) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(this.previousArrowId));
        }

        this.previousArrowId = this.entityArrow.getId();
    }

    protected void addLine(int index, String line) {
        this.hologram.addLine(index, line);
        for (UUID uuid : this.getCurrentlyViewing()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                continue;
            }
            this.hologram.setup(player);
        }
    }

    protected void removeLine(int index) {
        this.hologram.removeLine(index);
        for (UUID uuid : this.getCurrentlyViewing()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                continue;
            }
            this.hologram.update(player);
        }
    }

    protected void setMojangSkin(MojangSkin skin) {
        this.mojangSkin = skin;
        if (skin != null) {
            this.updateSkin(skin.value(), skin.signature());
        }
    }

    protected NPCEntry toEntry() {
        List<String> hologramLines = this.hologram.getLines().stream().map(HologramLine::getLine).toList();
        return new NPCEntry(this.getId(), this.getDisplayName(), this.getCommand(), this.getLocation(), this.inventory, this.mojangSkin, hologramLines, sitting);
    }
}

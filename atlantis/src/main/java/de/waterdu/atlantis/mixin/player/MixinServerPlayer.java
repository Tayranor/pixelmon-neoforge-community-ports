package de.waterdu.atlantis.mixin.player;

import com.mojang.authlib.GameProfile;
import de.waterdu.atlantis.permission.Permissions;
import de.waterdu.atlantis.ui.api.Page;
import de.waterdu.atlantis.util.entity.PlayerReference;
import de.waterdu.atlantis.util.java.interfaces.Lazy;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = ServerPlayer.class, remap = false)
public abstract class MixinServerPlayer extends Player {

    @Unique
    private final Lazy<PlayerReference> atlantis$reference = Lazy.of(() -> PlayerReference.get(this.uuid));

    public MixinServerPlayer(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    public PlayerReference reference() {
        return this.atlantis$reference.get();
    }

    public void openPage(Page page) {
        page.open(this);
    }

    public boolean hasPermission(String node) {
        return Permissions.hasPermission(this, node);
    }

    public <T> T hasPermission(PermissionNode<T> node) {
        return PermissionAPI.getPermission((ServerPlayer) (Object) this, node);
    }
}

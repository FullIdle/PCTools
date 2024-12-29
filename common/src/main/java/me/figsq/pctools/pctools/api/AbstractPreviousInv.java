package me.figsq.pctools.pctools.api;

import lombok.Getter;
import lombok.Setter;
import me.fullidle.ficore.ficore.common.api.ineventory.ListenerInvHolder;
import org.bukkit.inventory.Inventory;

@Getter
@Setter
public abstract class AbstractPreviousInv extends ListenerInvHolder {
    private Inventory previousInv;
}

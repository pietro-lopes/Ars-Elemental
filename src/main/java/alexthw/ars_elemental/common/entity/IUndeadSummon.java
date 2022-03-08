package alexthw.ars_elemental.common.entity;

import com.hollingsworth.arsnouveau.api.entity.ISummon;

public interface IUndeadSummon extends ISummon {

    default void inherit(ISummon living){
        int ticksLeft = Math.max(living.getTicksLeft(), 400);
        setTicksLeft(ticksLeft);
        setOwnerID(living.getOwnerID());
        living.setTicksLeft(0);
    }

}

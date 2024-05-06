package me.oliverhesse.damageoverhaul;

import org.bukkit.damage.DamageSource;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public enum DamageTypeEnum {
    SLASH((byte) 1){
        @Override
        String asString() {
            return "Slash";
        }
        },
    PIERCE((byte) 2){
        @Override
        String asString() {
            return "Pierce";
        }
        },
    BLUDGEON((byte) 4){
        @Override
        String asString() {
            return "Bludgeon";
        }
        },
    HEAT((byte) 8){
        @Override
        String asString() {
            return "Heat";
        }
        },
    BLEED((byte) 16){
        @Override
        String asString() {
            return "Bleed";
        }
        },
    BURN((byte) 32){
        @Override
        String asString() {
            return "Burn";
        }
        },
    ALMIGHTY((byte) 64){
        @Override
        String asString() {
            return "Almighty";
        }
    },
    NORMAL((byte) 128){
        @Override
        String asString() {
            return "Normal";
        }
    };
    abstract String asString();
    private final byte mask;

    DamageTypeEnum(byte mask) {
        this.mask = mask;
    }

    public byte getMask() {
        return mask;
    }
    //used to convert our bitmask into usable data
    public static List<DamageTypeEnum> getTypes(byte mask){
        List<DamageTypeEnum> toReturn = new ArrayList<>();
        for (DamageTypeEnum value : DamageTypeEnum.values()) {
            if ((mask & value.getMask()) != 0 ){
                toReturn.add(value);

            }
        }
        return toReturn;
    }
    public static DamageTypeEnum typeFromString (String typeName){

        return switch (typeName) {
            case "Bleed" -> DamageTypeEnum.BLEED;
            case "Slash" -> DamageTypeEnum.SLASH;
            case "Pierce" -> DamageTypeEnum.PIERCE;
            case "Bludgeon" -> DamageTypeEnum.BLUDGEON;
            case "Almighty" -> DamageTypeEnum.ALMIGHTY;
            case "Heat" -> DamageTypeEnum.HEAT;
            case "Burn" -> DamageTypeEnum.BURN;
            default -> DamageTypeEnum.NORMAL;
        };

    }

}

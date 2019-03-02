package fr.omegaserv.tower;

import org.bukkit.Location;

public class Cube
{
    Location l1;
    Location l2;
    private int xMin;
    private int xMax;
    private int yMin;
    private int yMax;
    private int zMin;
    private int zMax;

    public Cube(Location l1, Location l2)
    {
        if (l1.getBlockX() >= l2.getBlockX())
        {
            this.xMin = l2.getBlockX();
            this.xMax = l1.getBlockX();
        }
        else
        {
            this.xMax = l2.getBlockX();
            this.xMin = l1.getBlockX();
        }
        if (l1.getBlockY() >= l2.getBlockY())
        {
            this.yMin = l2.getBlockY();
            this.yMax = l1.getBlockY();
        }
        else
        {
            this.yMax = l2.getBlockY();
            this.yMin = l1.getBlockY();
        }
        if (l1.getBlockZ() >= l2.getBlockZ())
        {
            this.zMin = l2.getBlockZ();
            this.zMax = l1.getBlockZ();
        }
        else
        {
            this.zMax = l2.getBlockZ();
            this.zMin = l1.getBlockZ();
        }
    }

    public boolean isInLocation(Location l)
    {
        return (this.xMin <= l.getBlockX()) && (l.getBlockX() <= this.xMax) &&
                (this.yMin <= l.getBlockY()) && (l.getBlockY() <= this.yMax) &&
                (this.zMin <= l.getBlockZ()) && (l.getBlockZ() <= this.zMax);
    }
}

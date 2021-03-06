package dev.theturkey.videogames.games;

import dev.theturkey.videogames.VGCore;
import dev.theturkey.videogames.util.Vector2I;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

public class BrickBreakerGame extends VideoGameBase
{
	private static final int DIST_FROM_PLAYER = 30;
	private static final int Y = 75;
	private ArmorStand paddle;
	private int lookingTask = -1;

	private int[][] blocks = new int[][]{
			{5, 4, 3, 2, 1, 2, 3, 4, 5},
			{5, 4, 3, 2, 1, 2, 3, 4, 5},
			{5, 4, 3, 2, 1, 2, 3, 4, 5},
			{5, 4, 3, 2, 1, 2, 3, 4, 5},
			{5, 4, 3, 2, 1, 2, 3, 4, 5}
	};

	public BrickBreakerGame(Vector2I gameLoc)
	{
		super(gameLoc);
	}

	@Override
	public void constructGame(World world, Player player)
	{
		Vector2I gameLoc = getGameLocScaled();
		world.getBlockAt(new Location(world, gameLoc.getX(), Y, gameLoc.getY())).setType(Material.BEDROCK);

		player.teleport(new Location(world, gameLoc.getX() + 0.5, Y + 1, gameLoc.getY() + 0.5, 0, 0), PlayerTeleportEvent.TeleportCause.COMMAND);
		Location paddleLoc = new Location(world, gameLoc.getX() + 0.5, Y - 4, gameLoc.getY() + 0.5 + DIST_FROM_PLAYER, 0, 0);
		paddle = (ArmorStand) world.spawnEntity(paddleLoc, EntityType.ARMOR_STAND);
		paddle.setVisible(false);
		paddle.getEquipment().setItemInOffHand(new ItemStack(Material.STONE_SLAB));
		paddle.setLeftArmPose(new EulerAngle(-Math.PI / 16, Math.PI / 4, 0));
		paddle.getEquipment().setItemInMainHand(new ItemStack(Material.STONE_SLAB));
		paddle.setRightArmPose(new EulerAngle(-Math.PI / 16, Math.PI / 4, 0));
		paddle.setGravity(false);

		for(int x = -10; x < 10; x++)
		{
			for(int yy = -15; yy < 25; yy++)
			{
				world.getBlockAt(gameLoc.getX() + x, Y + yy, gameLoc.getY() + DIST_FROM_PLAYER + 1).setType(Material.BLACK_CONCRETE);
				if(x >= -9)
				{
					int colIndex = (x + 9) / 2;
					int rowIndex = (yy - 15) / 2;
					if(yy - 13 > 0 && yy - 13 < 10 && yy % 2 == 0 && colIndex < blocks[rowIndex].length)
					{
						int hitPoints = blocks[rowIndex][colIndex];
						world.getBlockAt(gameLoc.getX() + x, Y + yy, gameLoc.getY() + DIST_FROM_PLAYER).setType(getMatForHP(hitPoints));
					}
				}
			}
		}
	}

	public void startGame(World world, Player player)
	{
		lookingTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(VGCore.getPlugin(), () ->
		{
			float yaw = player.getLocation().getYaw();
			Location newLoc = paddle.getLocation().clone();
			double newX = Math.max(-9, Math.min(9, (DIST_FROM_PLAYER * Math.tan(Math.toRadians(yaw))) - 0.5));
			newLoc.setX(getGameLocScaled().getX() - newX);
			paddle.teleport(newLoc);
		}, 0, 1);
	}

	public void endGame(World world, Player player)
	{
		Bukkit.getScheduler().cancelTask(lookingTask);
	}

	@Override
	public void deconstructGame(World world, Player player)
	{
		Vector2I gameLoc = getGameLocScaled();
		world.getBlockAt(new Location(world, gameLoc.getX(), 49, gameLoc.getY())).setType(Material.AIR);
		paddle.remove();

		for(int x = -10; x < 10; x++)
		{
			for(int yy = -15; yy < 25; yy++)
			{
				world.getBlockAt(gameLoc.getX() + x, Y + yy, gameLoc.getY() + DIST_FROM_PLAYER + 1).setType(Material.AIR);
				world.getBlockAt(gameLoc.getX() + x, Y + yy, gameLoc.getY() + DIST_FROM_PLAYER).setType(Material.AIR);
			}
		}
	}

	public Material getMatForHP(int hitPoints)
	{
		switch(hitPoints)
		{
			case 1:
				return Material.RED_CONCRETE;
			case 2:
				return Material.ORANGE_CONCRETE;
			case 3:
				return Material.YELLOW_CONCRETE;
			case 4:
				return Material.BLUE_CONCRETE;
			case 5:
				return Material.GREEN_CONCRETE;
			default:
				return Material.WHITE_CONCRETE;
		}
	}
}

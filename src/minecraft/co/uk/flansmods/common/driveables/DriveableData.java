package co.uk.flansmods.common.driveables;

import java.util.HashMap;

import co.uk.flansmods.common.ItemPart;
import co.uk.flansmods.common.PartType;
import co.uk.flansmods.common.guns.ItemBullet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class DriveableData implements IInventory
{
	public int numGuns;
	public int numBombs;
	public int numCargo;
	public String type;
	public PartType engine;
	public ItemStack[] ammo;
	public ItemStack[] bombs;
	public ItemStack[] cargo;
	public ItemStack fuel;
	public float fuelInTank;
	/** Each driveable part has a small class that holds its current status */
	public HashMap<EnumDriveablePart, DriveablePart> parts;
		
	public DriveableData(NBTTagCompound tags)
	{
		parts = new HashMap<EnumDriveablePart, DriveablePart>();
		readFromNBT(tags);
	}

	public void readFromNBT(NBTTagCompound tag)
    {
		if(tag == null)
			return;
    	if(!tag.hasKey("Type"))
    		return;
    	
		type = tag.getString("Type");
		DriveableType dType = DriveableType.getDriveable(type);
		numBombs = dType.numBombSlots;
		numCargo = dType.numCargoSlots;
		numGuns = dType.numPassengerGunners + dType.guns.size();
		engine = PartType.getPart(tag.getString("Engine"));
		ammo = new ItemStack[numGuns];
		bombs = new ItemStack[numBombs];
		cargo = new ItemStack[numCargo];
		for(int i = 0; i < numGuns; i++)
		{
			ammo[i] = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Ammo " + i));
		}
		for(int i = 0; i < numBombs; i++)
		{
			bombs[i] = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Bombs " + i));
		}
 		for(int i = 0; i < numCargo; i++)
		{
			cargo[i] = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Cargo " + i));
		}
		fuel = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Fuel"));
		fuelInTank = tag.getInteger("FuelInTank");
		for(EnumDriveablePart part : EnumDriveablePart.values())
		{
			parts.put(part, new DriveablePart(part, dType.health.get(part)));
		}
		for(DriveablePart part : parts.values())
		{
			part.readFromNBT(tag);
		}
    }

    public void writeToNBT(NBTTagCompound tag)
    {
		tag.setString("Type", type);
		tag.setString("Engine", engine.shortName);
		for(int i = 0; i < ammo.length; i++)
		{
			if(ammo[i] != null)
				tag.setCompoundTag("Ammo " + i, ammo[i].writeToNBT(new NBTTagCompound()));
		}
		for(int i = 0; i < bombs.length; i++)
		{
			if(bombs[i] != null)
				tag.setCompoundTag("Bombs " + i, bombs[i].writeToNBT(new NBTTagCompound()));
		}
		for(int i = 0; i < cargo.length; i++)
		{
			if(cargo[i] != null)
				tag.setCompoundTag("Cargo " + i, cargo[i].writeToNBT(new NBTTagCompound()));
		}
		if(fuel != null)
			tag.setCompoundTag("Fuel", fuel.writeToNBT(new NBTTagCompound()));
		tag.setInteger("FuelInTank", (int)fuelInTank);
		for(DriveablePart part : parts.values())
		{
			part.writeToNBT(tag);
		}
    }
	
	@Override
	public int getSizeInventory() 
	{ 
		return getFuelSlot() + 1; 
	}

    @Override
	public ItemStack getStackInSlot(int i) 
	{ 
		//Find the correct inventory
		ItemStack[] inv = ammo;
		if(i >= ammo.length)
		{
			i -= ammo.length;
			inv = bombs;
			if(i >= bombs.length)
			{
				i -= bombs.length;
				inv = cargo;
				if(i >= cargo.length)
				{
					return fuel;
				}
			}	
		}
		//Return the stack in the slot
		return inv[i];
	}

    @Override
	public ItemStack decrStackSize(int i, int j) 
	{
		//Find the correct inventory
		ItemStack[] inv = ammo;
		if(i >= ammo.length)
		{
			i -= ammo.length;
			inv = bombs;
			if(i >= bombs.length)
			{
				i -= bombs.length;
				inv = cargo;
				if(i >= cargo.length)
				{
					//Put the fuel stack in a stack array just to simplify the code
					i -= cargo.length;
					inv = new ItemStack[1];
					inv[0] = fuel;		

					setInventorySlotContents(getFuelSlot(), null);
				}
			}	
		}
		//Decrease the stack size
		if(inv[i] != null)
        {
            if(inv[i].stackSize <= j)
            {
                ItemStack itemstack = inv[i];
                inv[i] = null;
                return itemstack;
            }
            ItemStack itemstack1 = inv[i].splitStack(j);
            if(inv[i].stackSize <= 0)
            {
                inv[i] = null;
            }
            return itemstack1;
        } else
        {
            return null;
        }
		
	}

    @Override
	public ItemStack getStackInSlotOnClosing(int i) 
	{ 
		return getStackInSlot(i);	
	}

    @Override
	public void setInventorySlotContents(int i, ItemStack stack) 
	{ 
		//Find the correct inventory
		ItemStack[] inv = ammo;
		if(i >= ammo.length)
		{
			i -= ammo.length;
			inv = bombs;
			if(i >= bombs.length)
			{
				i -= bombs.length;
				inv = cargo;
				if(i >= cargo.length)
				{
					fuel = stack;
					return;
				}
			}	
		}
		//Set the stack
		inv[i] = stack;
	}

    @Override
	public String getInvName() 
	{ 
		return "Flan's Secret Data"; 
	}

    @Override
	public int getInventoryStackLimit() 
	{ 
		return 64; 
	}

    @Override
	public void onInventoryChanged() {}

    @Override
	public boolean isUseableByPlayer(EntityPlayer player) 
	{ 
		return true; 
	}

    @Override
	public void openChest() {}

    @Override
	public void closeChest() {}
	
	public int getAmmoInventoryStart()
	{
		return 0;
	}
	
	public int getBombInventoryStart()
	{
		return ammo.length;
	}	
	
	public int getCargoInventoryStart()
	{
		return ammo.length + bombs.length;
	}
	
	public int getFuelSlot()
	{
		return ammo.length + bombs.length + cargo.length;
	}

	@Override
	public boolean isInvNameLocalized() 
	{
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) 
	{
		if(i < getBombInventoryStart() && itemstack != null && itemstack.getItem() instanceof ItemBullet) //Ammo
		{
			return true;
		}		
		if(i >= getBombInventoryStart() && i < getCargoInventoryStart() && itemstack != null && itemstack.getItem() instanceof ItemBullet) //Ammo
		{
			return true;
		}
		if(i >= getCargoInventoryStart() && i < getFuelSlot())
		{
			return true;
		}
		if(i == getFuelSlot() && itemstack != null && itemstack.getItem() instanceof ItemPart && ((ItemPart)itemstack.getItem()).type.category == 9) //Fuel
		{
			return true;
		}

		return false;
	}
}

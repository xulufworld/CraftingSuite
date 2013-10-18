package bau5.mods.craftingsuite.common.tileentity;

import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import bau5.mods.craftingsuite.common.ModificationNBTHelper;
import bau5.mods.craftingsuite.common.tileentity.parthandlers.ContainerHandler;
import bau5.mods.craftingsuite.common.tileentity.parthandlers.InventoryHandler;
import bau5.mods.craftingsuite.common.tileentity.parthandlers.ModdedTableInfo;

public class TileEntityModdedTable extends TileEntity{
	
	private byte[] upgrades = null;
	private final InventoryHandler inventoryHandler;
	private final ContainerHandler containerHandler;
	
	private ModdedTableInfo modifications;
	
	private boolean initialized = false;
	
	public TileEntityModdedTable(){
		inventoryHandler = new InventoryHandler(this);
		containerHandler = new ContainerHandler(this);
	}
	
	@Override
	public void updateEntity() {
		if(!(inventoryHandler == null || containerHandler == null)){
 			if(inventoryHandler().shouldUpdate && !containerHandler().isContainerInit() && !containerHandler().isContainerWorking()){
				inventoryHandler().findRecipe(false);
				inventoryHandler().shouldUpdate = false;
			}
		}
		super.updateEntity();
	}
	
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		writeToNBT(tag);
		Packet132TileEntityData packet = new Packet132TileEntityData(xCoord, yCoord, zCoord, 1, tag);
		return packet;
	}
	
	@Override
	public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt) {
		readFromNBT(pkt.data);
		super.onDataPacket(net, pkt);
	}

	@Override
	public void onInventoryChanged() {
		inventoryHandler.onTileInventoryChanged();
		super.onInventoryChanged();
	}
	
	public InventoryHandler inventoryHandler(){
		return inventoryHandler;
	}
	
	public ContainerHandler containerHandler(){
		return containerHandler;
	}
	
	public ModdedTableInfo modifications(){
		return modifications;
	}
	
	public void initializeFromNBT(NBTTagList tag){
		if(!initialized){
			upgrades = ModificationNBTHelper.getUpgradeByteArray(tag).byteArray;
			init();
			this.loadAdditionalNBT(tag);
		}
	}
	
	public void loadAdditionalNBT(NBTTagList tag) {
		
	}
	
	public void init(){
		if(!initialized){
			modifications = new ModdedTableInfo(upgrades);
			inventoryHandler().initInventory();
			initialized = true;
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tagCompound)
	{
		super.readFromNBT(tagCompound);
		NBTTagList modInfoList = ModificationNBTHelper.getModInfoList(tagCompound);
		for(int i = 0; i < modInfoList.tagCount(); i++){
			if(i == 0)
				upgrades = ModificationNBTHelper.getUpgradeByteArray(modInfoList).byteArray;
		}
		init();
		inventoryHandler.readInventoryFromNBT(tagCompound);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tagCompound)
	{
		super.writeToNBT(tagCompound);
		inventoryHandler.writeInventoryToNBT(tagCompound);
		
		NBTTagList modInfoList = new NBTTagList();
		for(int i = 0; i < 1; i++){
			if(i == 0){
				NBTTagByteArray bytes =  new NBTTagByteArray(ModificationNBTHelper.upgradeArrayName);
				if(upgrades != null){
					bytes.byteArray = upgrades;
				}else
					bytes.byteArray = new byte[0];
				modInfoList.appendTag(bytes);
			}
		}
		tagCompound.setTag(ModificationNBTHelper.tagListName, modInfoList);
	}

	public byte[] getUpgrades() {
		return upgrades;
	}
}
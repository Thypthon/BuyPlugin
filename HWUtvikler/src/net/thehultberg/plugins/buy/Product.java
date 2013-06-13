package net.thehultberg.plugins.buy;

public class Product {

	private String customName;
	private int price;
	private int perPrice;
	private int itemId;
	private short damage;
	
	/**
	 * Lager et produkt.
	 * @param arg0 (<code>String</code>) Navnet, ikke formatert.
	 * @param arg1 (<code><b>int</b></code>) Pris.
	 * @param arg2 (<code><b>int</b></code>) Får per.
	 * @param arg3 (<code><b>int</b></code>) ID på item.
	 * @param arg4 (<code><b>short</b></code>) Damage på item.
	 */
	public Product(String arg0, int arg1, int arg2, int arg3, short arg4){
		this.customName = arg0;
		this.price = arg1;
		this.perPrice = arg2;
		this.itemId = arg3;
		this.damage = arg4;
	}

	public String getCustomName() {
		return customName;
	}

	public void setCustomName(String customName) {
		this.customName = customName;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getPerPrice() {
		return perPrice;
	}

	public void setPerPrice(int perPrice) {
		this.perPrice = perPrice;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public short getDamage() {
		return damage;
	}

	public void setDamage(short damage) {
		this.damage = damage;
	}
	
}

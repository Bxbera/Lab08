package it.polito.tdp.extflightdelays.model;

public class CoppiaA {

	private Airport partenza;
	private Airport destinazione;
	private double distanzaMedia;
	
	public CoppiaA(Airport partenza, Airport destinazione, double distanzaMedia) {
		super();
		this.partenza = partenza;
		this.destinazione = destinazione;
		this.distanzaMedia = distanzaMedia;
	}

	public Airport getPartenza() {
		return partenza;
	}

	public Airport getDestinazione() {
		return destinazione;
	}

	public double getDistanzaMedia() {
		return distanzaMedia;
	}
	
	
	
}

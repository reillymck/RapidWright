/*
 * 
 * Copyright (c) 2017 Xilinx, Inc. 
 * All rights reserved.
 *
 * Author: Chris Lavin, Xilinx Research Labs.
 *
 * This file is part of RapidWright. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
/**
 * 
 */
package com.xilinx.rapidwright.edif;

import java.util.List;

import com.xilinx.rapidwright.design.Cell;
import com.xilinx.rapidwright.design.Design;
import com.xilinx.rapidwright.design.SitePinInst;
import com.xilinx.rapidwright.device.Node;

/**
 * Combines an {@link EDIFHierPortInst} with a full hierarchical
 * instance name to uniquely identify a port instance in a netlist.
 * 
 * Created on: Sep 12, 2017
 */
public class EDIFHierPortInst {

	private String hierarchicalInstName;
	
	private EDIFPortInst portInst;

	/**
	 * Constructor
	 * @param hierarchicalInstName The hierarchical parent instance cell name of the port 
	 * @param portInst The actual port ref object
	 */
	public EDIFHierPortInst(String hierarchicalInstName, EDIFPortInst portInst) {
		super();
		this.hierarchicalInstName = hierarchicalInstName;
		this.portInst = portInst;
	}

	/**
	 * The name of the parent instance cell that contains the instance
	 * cell pin.
	 * @return the hierarchicalInstanceName
	 */
	public String getHierarchicalInstName() {
		return hierarchicalInstName;
		
	}

	/**
	 * Gets the net on the port inst
	 * @return The net on the port inst
	 */
	public EDIFNet getNet() {
	    if(portInst == null) return null;
	    return portInst.getNet();
	}
	
	/**
	 * Returns the full hierarchical name of the instance on which this port resides.
	 * @return The full hierarchical name.
	 */
	public String getFullHierarchicalInstName(){
		if(portInst.getCellInst() == null){
			// Internal (inward-facing) side of a cell port
			return hierarchicalInstName;
		}
		EDIFCellInst topCellInst = portInst.getCellInst().getCellType().getLibrary().getNetlist().getTopCellInst();
		if(hierarchicalInstName.isEmpty())
			return portInst.getCellInst().equals(topCellInst) ? "" : portInst.getCellInst().getName(); 
		return hierarchicalInstName + EDIFTools.EDIF_HIER_SEP + portInst.getCellInst().getName();
	}
	
	/**
	 * @param hierarchicalInstanceName the hierarchicalInstanceName to set
	 */
	public void setHierarchicalInstName(String hierarchicalInstanceName) {
		this.hierarchicalInstName = hierarchicalInstanceName;
	}

	/**
	 * @return the portInst
	 */
	public EDIFPortInst getPortInst() {
		return portInst;
	}

	public EDIFCell getCellType() {
		if(portInst == null) return null;
		if(portInst.getCellInst() == null) return null;
		return portInst.getCellInst().getCellType();
	}
	
	/**
	 * @param portInst the port instance to set
	 */
	public void setPortInst(EDIFPortInst portInst) {
		this.portInst = portInst;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hierarchicalInstName == null) ? 0 : hierarchicalInstName.hashCode());
		result = prime * result + ((portInst == null) ? 0 : portInst.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EDIFHierPortInst other = (EDIFHierPortInst) obj;
		if (hierarchicalInstName == null) {
			if (other.hierarchicalInstName != null)
				return false;
		} else if (!hierarchicalInstName.equals(other.hierarchicalInstName))
			return false;
		if (portInst == null) {
			if (other.portInst != null)
				return false;
		} else if (!portInst.equals(other.portInst))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if(hierarchicalInstName.isEmpty()) return portInst.getFullName();
		return hierarchicalInstName + "/" + portInst.getFullName();
	}

	public String getHierarchicalNetName(){
		if(hierarchicalInstName.isEmpty()) return portInst.getNet().getName();
		return hierarchicalInstName + EDIFTools.EDIF_HIER_SEP + portInst.getNet();
	}
	
	public String getTransformedNetName(){
		String portName = null;
		if(portInst.getPort().getWidth() > 1){
			EDIFCellInst eci = portInst.getCellInst();
			int idx = portInst.getIndex();
			if(portInst.getPort().isLittleEndian()){
				idx = (portInst.getPort().getWidth()-1) - idx;
			}
			portName = portInst.getPort().getBusName() + idx;
			if(eci != null) 
				portName = portInst.getCellInst().getName() + EDIFTools.EDIF_HIER_SEP + portName;  
		}else{
			portName = portInst.getFullName();
		}
		if(hierarchicalInstName.isEmpty()) return portName;
		return hierarchicalInstName + "/" + portName;
	}
	
	public boolean isOutput(){
		return portInst.getPort().isOutput();
	}
	
	public boolean isInput(){
		return portInst.getPort().isInput();
	}
	
	/**
	 * Gets the routed site pin if this port is on a placed leaf cell and its' site is routed
	 * @param design The current design
	 * @return The connected site pin to the connected to this cell pin.
	 */
	public SitePinInst getRoutedSitePinInst(Design design) {
		String cellName = getFullHierarchicalInstName();
		Cell cell = design.getCell(cellName);
		if(cell == null) return null;
		return cell.getSitePinFromPortInst(getPortInst(), null);
	}
	
	/**
	 * Gets the list of site pins if this port is on a placed leaf cell and its' site is routed
	 * @param design The current design
	 * @return The list of connected site pins to the connected to this cell pin.
	 */
	public List<SitePinInst> getAllRoutedSitePinInsts(Design design) {
		String cellName = getFullHierarchicalInstName();
		Cell cell = design.getCell(cellName);
		if(cell == null) return null;
		return cell.getAllSitePinsFromPortInst(getPortInst(), null);
	}
}

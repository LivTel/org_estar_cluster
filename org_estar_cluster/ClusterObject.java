// ClusterObject.java
package org.estar.cluster;

import java.lang.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import org.estar.astrometry.*;

/**
 * This class extends org.estar.astrometry.CelestialObject, for containing the extra data contained
 * in a Cluster star object.
 * @author Chris Mottram
 * @version $Revision: 1.2 $
 * @see org.estar.astrometry.CelestialObject
 */
public class ClusterObject extends CelestialObject
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id: ClusterObject.java,v 1.2 2003-02-27 20:36:10 cjm Exp $";
	/**
	 * Token index.
	 */
	private final static int TOKEN_INDEX_FIELD_NUMBER = 0;
	private final static int TOKEN_INDEX_STAR_NUMBER = 1;
	private final static int TOKEN_INDEX_RAH = 2;
	private final static int TOKEN_INDEX_RAM = 3;
	private final static int TOKEN_INDEX_RAS = 4;
	private final static int TOKEN_INDEX_DECD = 5;
	private final static int TOKEN_INDEX_DECM = 6;
	private final static int TOKEN_INDEX_DECS = 7;
	private final static int TOKEN_INDEX_X_POS = 8;
	private final static int TOKEN_INDEX_Y_POS = 9;
	private final static int TOKEN_INDEX_MAGNITUDE_DATA_START = 10;
	private final static int TOKEN_INDEX_OFFSET_MAGNITUDE = 0;
	private final static int TOKEN_INDEX_OFFSET_ERROR = 1;
	private final static int TOKEN_INDEX_OFFSET_FLAG = 2;
	private final static int TOKEN_MAGNITUDE_DATA_COUNT = 3;
	/**
	 * Field number.
	 */
	protected int fieldNumber;
	/**
	 * Star number.
	 */
	protected int starNumber;
	/**
	 * X pixel position.
	 */
	protected double xPixel;
	/**
	 * Y pixel position.
	 */
	protected double yPixel;
	protected int magnitudeCount;
	protected double magnitudeArray[];
	protected double magnitudeErrorArray[];
	protected int flagsArray[];

	/**
	 * Default constructor.
	 */
	public ClusterObject()
	{
		super();
	}

	public int getFieldNumber()
	{
		return fieldNumber;
	}

	public int getStarNumber()
	{
		return starNumber;
	}

	public double getXPixel()
	{
		return xPixel;
	}

	public double getYPixel()
	{
		return yPixel;
	}

	public int getMagnitudeCount()
	{
		return magnitudeCount;
	}

	public double getMagnitude(int index)
	{
		return magnitudeArray[index];
	}

	public double getMagnitudeError(int index)
	{
		return magnitudeErrorArray[index];
	}

	public int getFlags(int index)
	{
		return flagsArray[index];
	}

	/**
	 * Method to parse a star line in the catalogue, of the form:
	 * <pre>
	 * 0       1  08 20 41.696 +13 58 50.58   967.449   274.266    -14.490      0.003   05
	 * </pre>
	 * However, some fake/test catalogues do not have a [+|-] sign in front of the degrees,
	 * so we must watch for this and add the sign. e.g.:
	 * <pre>
	 *    1      1   01 10 12.71   60 04 14.37   254.180   142.627     18.810      0.056   00
	 * </pre>
	 * The space-separated fields are:
	 * <ul>
	 * <li>field number.
	 * <li>star number.
	 * <li>RA hours.
	 * <li>RA minutes.
	 * <li>RA seconds.
	 * <li>Dec degress (note -00 error).
	 * <li>Dec minutes.
	 * <li>Dec seconds.
	 * <li>X pixel position.
	 * <li>Y pixel position.
	 * </ul>
	 * And then, for each colour defined:
	 * <ul>
	 * <li>Magnitude.
	 * <li>Error.
	 * <li>Flags.
	 * </ul>
	 * @param s The string to parse.
	 * @param colourCount The number of magnitudes in the list.
	 * @param colourNameStringList The names of the magnitudes.
	 * @exception IllegalArgumentException Thrown if an argument was out of bounds.
	 * @see #fieldNumber
	 * @see #starNumber
	 * @see #xPixel
	 * @see #yPixel
	 * @see #magnitudeArray
	 * @see #magnitudeErrorArray
	 * @see #flagsArray
	 * @see #TOKEN_INDEX_FIELD_NUMBER
	 * @see #TOKEN_INDEX_STAR_NUMBER
	 * @see #TOKEN_INDEX_RAH
	 * @see #TOKEN_INDEX_RAM
	 * @see #TOKEN_INDEX_RAS
	 * @see #TOKEN_INDEX_DECD
	 * @see #TOKEN_INDEX_DECM
	 * @see #TOKEN_INDEX_DECS
	 * @see #TOKEN_INDEX_X_POS
	 * @see #TOKEN_INDEX_Y_POS
	 * @see #TOKEN_INDEX_MAGNITUDE_DATA_START
	 * @see #TOKEN_INDEX_OFFSET_MAGNITUDE
	 * @see #TOKEN_INDEX_OFFSET_ERROR
	 * @see #TOKEN_INDEX_OFFSET_FLAG
	 * @see #TOKEN_MAGNITUDE_DATA_COUNT
	 */
	public void parseStarLine(String s,int colourCount,String colourNameStringList[]) 
		throws IllegalArgumentException
	{
		RA ra = null;
		Dec dec = null;
		StringTokenizer st = null;
		String tokenString = null;
		int index,rah=0,ram=0,decd=0,decm=0,magArrayIndex,indexOffset;
		double ras,decs;
		char signChar = ' ';

		// setup arrays
		magnitudeCount = colourCount;
		magnitudeArray = new double[magnitudeCount];
		magnitudeErrorArray = new double[magnitudeCount];
		flagsArray = new int[magnitudeCount];
		// parse string
		index = 0;
		st = new StringTokenizer(s," ");
		while(st.hasMoreTokens())
		{
			// get this token
			tokenString = st.nextToken();
			switch(index)
			{
			case TOKEN_INDEX_FIELD_NUMBER:
				fieldNumber = Integer.parseInt(tokenString);
				break;
			case TOKEN_INDEX_STAR_NUMBER:
				starNumber = Integer.parseInt(tokenString);
				break;
			case TOKEN_INDEX_RAH:
				rah = Integer.parseInt(tokenString);
				break;
			case TOKEN_INDEX_RAM:
				ram = Integer.parseInt(tokenString);
				break;
			case TOKEN_INDEX_RAS:
				ras = Double.parseDouble(tokenString);
				ra = new RA(rah,ram,ras);
				setRA(ra);
				break;
			case TOKEN_INDEX_DECD:
				signChar = tokenString.charAt(0);
				// if sign char was a plus or minus, dec degrees is rest of tokenString
				// if sign char was not +|-, assume '+' and parse whole tokenString
				if((signChar == '+')|| (signChar == '-'))
				{
					tokenString = tokenString.substring(1,tokenString.length());
				}
				else
					signChar = '+';
				decd = Integer.parseInt(tokenString);
				break;
			case TOKEN_INDEX_DECM:
				decm = Integer.parseInt(tokenString);
				break;
			case TOKEN_INDEX_DECS:
				decs = Double.parseDouble(tokenString);
				dec = new Dec(signChar,decd,decm,decs);
				setDec(dec);
				break;
			case TOKEN_INDEX_X_POS:
				xPixel = Double.parseDouble(tokenString);
				break;
			case TOKEN_INDEX_Y_POS:
				yPixel = Double.parseDouble(tokenString);
				break;
			default:
				magArrayIndex = (index-TOKEN_INDEX_MAGNITUDE_DATA_START)/TOKEN_MAGNITUDE_DATA_COUNT;
				indexOffset = (index-TOKEN_INDEX_MAGNITUDE_DATA_START) % TOKEN_MAGNITUDE_DATA_COUNT;
				if(magArrayIndex >= magnitudeCount)
				{
					throw new IllegalArgumentException(this.getClass().getName()+
					    "parseStarLine:Magnitude Array Index out of bounds:"+index+":"+
					    magnitudeCount+":"+tokenString);
				}
				if(indexOffset == TOKEN_INDEX_OFFSET_MAGNITUDE)
				{
					magnitudeArray[magArrayIndex] = Double.parseDouble(tokenString);
					if(colourNameStringList[magArrayIndex].equalsIgnoreCase("B"))
						setBMagnitude(magnitudeArray[magArrayIndex]);
					if(colourNameStringList[magArrayIndex].equalsIgnoreCase("V"))
						setVMagnitude(magnitudeArray[magArrayIndex]);
					if(colourNameStringList[magArrayIndex].equalsIgnoreCase("R"))
						setRMagnitude(magnitudeArray[magArrayIndex]);
				}
				else if(indexOffset == TOKEN_INDEX_OFFSET_ERROR)
					magnitudeErrorArray[magArrayIndex] = Double.parseDouble(tokenString);
				else if(indexOffset == TOKEN_INDEX_OFFSET_FLAG)
					flagsArray[magArrayIndex] = Integer.parseInt(tokenString);
				else
				{
					throw new IllegalArgumentException(this.getClass().getName()+
					   "parseStarLine:Index Offset out of bounds:"+indexOffset+":"+tokenString);
				}
				break;
			}
			// increment index
			index++;
		}
	}

	public void write(Writer w) throws IOException
	{
		w.write(this.toString());
		w.write("\n");
	}

	public String toString()
	{
		DecimalFormat df = null;
		DecimalFormat dfd = null;
		StringBuffer sb = null;
		int index;

		df = new DecimalFormat("00");
		dfd = new DecimalFormat("0.000");
		sb = new StringBuffer();
		sb.append(""+fieldNumber+" "+starNumber+" "+ra.toString(' ')+" "+dec.toString(' ')+" "+
			  dfd.format(xPixel)+" "+dfd.format(yPixel));
		for(index = 0;index < magnitudeCount; index ++)
		{
			sb.append(" "+magnitudeArray[index]+" "+magnitudeErrorArray[index]+" "+
				  df.format(flagsArray[index]));
		}
		return sb.toString();
	}
}
/*
** $Log: not supported by cvs2svn $
** Revision 1.1  2002/12/29 22:03:49  cjm
** Initial revision
**
*/

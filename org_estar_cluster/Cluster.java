// Cluster.java
package org.estar.cluster;

import java.lang.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import org.estar.astrometry.*;

/**
 * This class loads a cluster format file from disk or URL, into an instance of this class.
 * Cluster format files contain a list of stars, their position, in RA/DEC and pixel positions.
 * It can also contain magnitude information.
 * @author Chris Mottram
 * @version $Revision: 1.3 $
 * @see ClusterObject
 */
public class Cluster
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id: Cluster.java,v 1.3 2005-05-23 13:39:02 cjm Exp $";
	/**
	 * The number of colours in the catalogue.
	 */
	protected int colourCount;
	/**
	 * The names of the magnitude data.
	 */
	protected String colourNameList[];
	/**
	 * The comment string after the number of colours, if any. This is in line 1 of the cluster file.
	 */
	protected String colourCommentString = null;
	/**
	 * The comment, if any. This is in line 3 of the cluster file.
	 */
	protected String commentString = null;
	/**
	 * The list of stars.
	 */
	protected List starList = null;

	/**
	 * Default constructor.
	 */
	public Cluster()
	{
		super();
	}

	/**
	 * Method to load a cluster file from a buffered reader.
	 * @param r The reader to use.
	 * @exception IOException Thrown if something couldn't be read.
	 * @see #parseColourCountLine
	 * @see #parseColourNameListLine
	 * @see #parseStarLine
	 * @see #commentString
	 */
	public void load(BufferedReader r) throws IOException
	{
		String s = null;
		boolean done;

		// read colour count
		s = r.readLine();
		if(s == null)
			throw new IOException("load failed:reading colour count returned null.");
		parseColourCountLine(s);
		// read colour name list
		s = r.readLine();
		if(s == null)
			throw new IOException("load failed:reading colour list returned null.");
		parseColourNameListLine(s);
		// read comment
		s = r.readLine();
		if(s == null)
			throw new IOException("load failed:reading comment returned null.");
		commentString = s;
		// read star list
		starList = new Vector();
		done = false;
		while ( done == false)
		{
			s = r.readLine();
			if( s != null)
				parseStarLine(s);
			else
				done = true;
		}
	}

	/**
	 * Method to save a cluster file to the specified Writer
	 * @param w The writer.
	 * @exception IOException Thrown if an IO error occurs.
	 * @see #writeColourCountLine
	 * @see #writeColourNameListLine
	 * @see ClusterObject#write
	 * @see #starList
	 * @see #commentString
	 */
	public void save(Writer w) throws IOException
	{
		Iterator iterator = null;
		ClusterObject clusterObject = null;

		writeColourCountLine(w);
		writeColourNameListLine(w);
		if(commentString != null)
			w.write(commentString+"\n");
		else
			w.write("\n");
		iterator = starList.iterator();
		while(iterator.hasNext())
		{
			clusterObject = (ClusterObject)(iterator.next());
			clusterObject.write(w);
		}
	}

	/**
	 * Method returns the list of stars.
	 * @return The list of stars.
	 * @see #starList
	 */
	public List getStarList()
	{
		return starList;
	}

	/**
	 * Method returns the number of stars in the list.
	 * @return The number of stars.
	 * @see #starList
	 */
	public int getStarListCount()
	{
		return starList.size();
	}

	/**
	 * Get information on the star at index in the cluster list.
	 * @param index The index in the list.
	 * @return Information on the specified star.
	 * @see #starList
	 * @see ClusterObject
	 */
	public ClusterObject getStar(int index)
	{
		return (ClusterObject)(starList.get(index));
	}

	/**
	 * Method to print out a string representation of this cluster file.
	 * @return The string.
	 * @see #toString(java.lang.String)
	 */
	public String toString()
	{
		return toString("");
	}

	/**
	 * Method to print the contents of a cluster file to a string.
	 * @param prefix A prefix string to prepend to every line.
	 * @see #writeColourCountLine
	 * @see #writeColourNameListLine
	 * @see ClusterObject#write
	 * @see #starList
	 * @see #commentString
	 */
	public String toString(String prefix)
	{
		StringBuffer sb = null;
		Iterator iterator = null;
		ClusterObject clusterObject = null;
		int index;

		sb = new StringBuffer();
		// colour line count
		sb.append(prefix+colourCount+" "+colourCommentString+"\n");
		// colour name list line
		sb.append(prefix);
		for(index = 0;index < colourCount;index++)
		{
			sb.append(colourNameList[index]);
			if(index < (colourCount-1))
				sb.append(" ");
		}
	        sb.append("\n");
		// comment
		if(commentString != null)
			sb.append(prefix+commentString+"\n");
		else
			sb.append(prefix+"\n");
		// list of stars
		iterator = starList.iterator();
		while(iterator.hasNext())
		{
			clusterObject = (ClusterObject)(iterator.next());
			sb.append(clusterObject.toString(prefix)+"\n");
		}
		return sb.toString();
	}

	/**
	 * Static method to load and instansiate a Cluster object from the specified file.
	 * @param file The file to load.
	 * @return An Cluster object instance.
	 * @exception FileNotFoundException Thrown if the file doesnot exist.
	 * @exception IOException Thrown if there is a problem with the load.
	 * @see #load(java.io.BufferedReader)
	 */
	public static Cluster load(File file) throws FileNotFoundException,IOException
	{
		Cluster c = null;
		FileInputStream fis = null;

		c = new Cluster();
		fis = new FileInputStream(file);
		c.load(new BufferedReader(new InputStreamReader(fis)));
		fis.close();
		return c;
	}

	/**
	 * Static method to load and instansiate a Cluster object from the specified URL.
	 * @param url The URL to load from.
	 * @return An Cluster object instance.
	 * @exception IOException Thrown if there is a problem with the load.
	 * @see #load(java.io.BufferedReader)
	 */
	public static Cluster load(URL url) throws IOException
	{
		Cluster c = null;
		InputStream is = null;

		c = new Cluster();
		is = url.openStream();
		c.load(new BufferedReader(new InputStreamReader(is)));
		is.close();
		return c;
	}

	/**
	 * Static method to load and instansiate a Cluster object from the specified string.
	 * @param s A string containing the contents of a cluster file.
	 * @return An Cluster object instance.
	 * @exception IOException Thrown if there is a problem with the load.
	 * @see #load(java.io.BufferedReader)
	 */
	public static Cluster load(String s) throws IOException
	{
		Cluster c = null;
		BufferedReader r = null;

		c = new Cluster();
		r = new BufferedReader(new StringReader(s));
		c.load(r);
		r.close();
		return c;
	}

	/**
	 * Method to parse the colour count line.
	 * The first space seperated token should be a number - the number of colours.
	 * A random string can follow this.
	 * @param s The string to parse.
	 * @see #colourCount
	 * @see #colourNameList
	 * @see #colourCommentString
	 */
	protected void parseColourCountLine(String s)
	{
		StringTokenizer st = null;
		String tokenString = null;

		colourCount = 0;
		colourNameList = null;
		colourCommentString = null;
		st = new StringTokenizer(s," ");
		if(st.hasMoreTokens())
		{
			tokenString = st.nextToken();
			colourCount = Integer.parseInt(tokenString);
			colourNameList = new String[colourCount];
		}
		while(st.hasMoreTokens())
		{
			tokenString = st.nextToken();
			if(colourCommentString == null)
				colourCommentString = tokenString;
			else
				colourCommentString = new String(colourCommentString+" "+tokenString);
		}
	}

	/**
	 * Method to parse the space-separated list of colour names.
	 * @param s The string to parse.
	 * @see #colourNameList
	 */
	protected void parseColourNameListLine(String s)
	{
		StringTokenizer st = null;
		String tokenString = null;
		int index;

		index = 0;
		st = new StringTokenizer(s," ");
		while(st.hasMoreTokens())
		{
			tokenString = st.nextToken();
			colourNameList[index] = tokenString;
			index++;
		}
	}
	
	/**
	 * Method to parse a star line in the catalogue. Creates a new ClusterObject and calls the parseStarLine
	 * method. The result is added to the starList.
	 * @param s The string to parse.
	 * @see #starList
	 * @see ClusterObject#parseStarLine
	 */
	protected void parseStarLine(String s)
	{
		ClusterObject co = null;

		co = new ClusterObject();
		co.parseStarLine(s,colourCount,colourNameList);
		starList.add(co);
	}

	/**
	 * Method to write out the colour count line.
	 * @param w The writer to write to.
	 * @exception IOException Thrown if the write fails.
	 * @see #colourCount
	 * @see #colourCommentString
	 */
	protected void writeColourCountLine(Writer w) throws IOException
	{
		w.write(""+colourCount+" "+colourCommentString+"\n");
	}

	/**
	 * Method to write out the colour name list.
	 * @param w The writer to write to.
	 * @exception IOException Thrown if the write fails.
	 * @see #colourCount
	 * @see #colourNameList
	 */
	protected void writeColourNameListLine(Writer w) throws IOException
	{
		int index;

		for(index = 0;index < colourCount;index++)
		{
			w.write(colourNameList[index]);
			if(index < (colourCount-1))
				w.write(" ");
		}
		w.write("\n");
	}

	/**
	 * Main program, to test loading Cluster files, and printing them out.
	 */
	public static void main(String args[])
	{
		Cluster cluster = null;
		Writer w = null;
		String filename = null;

		if(args.length < 1)
		{
			System.err.println("Cluster: java Cluster -help");
			System.exit(1);
		}
		for(int i = 0; i < args.length; i++)
		{
		    if(args[i].equals("-filename")||args[i].equals("-f"))
		    {
			if((i+1) < args.length)
			{
			    filename = args[i+1];
			    i++;
			}
			else
			    System.err.println("Cluster:-filename requires a filename.");
		    }
		    else if(args[i].equals("-help")||args[i].equals("-h"))
		    {
			System.out.println("Cluster Help");
			System.out.println("java Cluster -f[ilename] <file name>[-h[elp]]");
			System.exit(0);
		    }
		    else
			System.err.println("Cluster:"+args[i]+" not recognized.");
		}// end for		
		try
		{
			cluster = Cluster.load(new File(filename));
		}
		catch (Exception e)
		{
			System.err.println("Cluster: load failed:"+e);
			e.printStackTrace(System.err);
			System.exit(2);
		}
		w = new BufferedWriter(new OutputStreamWriter(System.out));
		try
		{
			cluster.save(w);
			w.close();
		}
		catch (Exception e)
		{
			System.err.println("Cluster: save to stdout failed:"+e);
			e.printStackTrace(System.err);
			System.exit(2);
		}
		System.exit(0);
	}
}
/*
** $Log: not supported by cvs2svn $
** Revision 1.2  2003/02/23 11:27:39  cjm
** Added load method from string.
**
** Revision 1.1  2002/12/29 22:03:49  cjm
** Initial revision
**
*/

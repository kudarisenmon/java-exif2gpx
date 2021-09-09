package com.jpn.senmon.exif2gpx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;

/**
 * exif2gpx
 *
 */
public class App {
	public static void main(String[] args) {
		Path startPath = Paths.get(args[0]);

		ExifFileVisitor efv = new ExifFileVisitor();
		try {
			Files.walkFileTree(startPath, efv);

			Collections.sort(efv.getWayPointList(), new java.util.Comparator<WayPoint>() {
				@Override
				public int compare(WayPoint wp1, WayPoint wp2) {
					int result = wp1.getTime().get().compareTo(wp2.getTime().get());
					if (result != 0) {
						return result;
					} else {
						return wp1.getSource().get().compareTo(wp2.getSource().get());
					}
				}
			});

			TrackSegment segment = TrackSegment.builder().points(efv.getWayPointList()).build();
			Track track = Track.builder().name("Track 1").desc("Create by exif2gpx").addSegment(segment).build();
			GPX gpx = GPX.builder().addTrack(track).build();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			GPX.write(gpx, baos);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(baos.toByteArray()));
			doc.setXmlStandalone(true);

			Transformer tf = TransformerFactory.newInstance().newTransformer();
			tf.setOutputProperty(OutputKeys.METHOD, "xml");
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

			tf.transform(new DOMSource(doc), new StreamResult(System.out));

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
}

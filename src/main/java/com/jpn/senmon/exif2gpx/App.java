package com.jpn.senmon.exif2gpx;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;

/**
 * Hello world!
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
			GPX.write(gpx, System.out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

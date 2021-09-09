package com.jpn.senmon.exif2gpx;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.lang.Rational;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.file.FileSystemDirectory;

import io.jenetics.jpx.WayPoint;

public class ExifFileVisitor implements FileVisitor<Path> {

	ArrayList<WayPoint> wayPointList = new ArrayList<WayPoint>();

	public ArrayList<WayPoint> getWayPointList() {
		return wayPointList;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		Metadata metadata = null;
		try {
			metadata = JpegMetadataReader.readMetadata(file.toFile());
			GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
			FileSystemDirectory fsDirectory = metadata.getFirstDirectoryOfType(FileSystemDirectory.class);
			if (gpsDirectory != null && fsDirectory != null) {
				GeoLocation latlon = gpsDirectory.getGeoLocation();
				Date date = gpsDirectory.getGpsDate();
				Rational alt = gpsDirectory.getRational(GpsDirectory.TAG_ALTITUDE);
				double ele = 0;
				if (alt != null) {
					ele = alt.doubleValue();
				}
				WayPoint point = WayPoint.builder().lat(latlon.getLatitude()).lon(latlon.getLongitude())
						.time(date.getTime()).ele(ele).src(fsDirectory.getString(FileSystemDirectory.TAG_FILE_NAME))
						.build();
				wayPointList.add(point);
			}
		} catch (JpegProcessingException | IOException e) {
			e.printStackTrace();
		}

		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		return FileVisitResult.CONTINUE;
	}

}

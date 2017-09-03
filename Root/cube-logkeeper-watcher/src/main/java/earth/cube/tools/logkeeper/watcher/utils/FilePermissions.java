package earth.cube.tools.logkeeper.watcher.utils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import earth.cube.tools.logkeeper.watcher.utils.jackson.FilePermissionsDeserializer;

public class FilePermissions {
	
	@JsonProperty("permissions")
	@JsonDeserialize(using=FilePermissionsDeserializer.class)
	private Set<PosixFilePermission> _perms;

	@JsonProperty("owner")
	private String _sOwner;

	@JsonProperty("group")
	private String _sGroup;
	
	
	public void apply(Path path) throws IOException {
		if(_perms != null)
			Files.setPosixFilePermissions(path, _perms);
		if((_sOwner != null && _sOwner.length() != 0) || (_sGroup != null && _sGroup.length() != 0)) {
			UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
	
			if(_sOwner != null && _sOwner.length() != 0) {
				UserPrincipal principal = lookupService.lookupPrincipalByName(_sOwner);
				Files.setOwner(path, principal);
			}
			
			if(_sGroup != null && _sGroup.length() != 0) {
				GroupPrincipal principal = lookupService.lookupPrincipalByGroupName(_sGroup);
				Files.getFileAttributeView(path, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS).setGroup(principal);
			}
		 
		}	
	}

}

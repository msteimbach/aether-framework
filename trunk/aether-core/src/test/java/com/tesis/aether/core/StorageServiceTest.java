package com.tesis.aether.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.tesis.aether.core.factory.ServiceAccountProperties;
import com.tesis.aether.core.services.storage.LocalStorageService;
import com.tesis.aether.core.services.storage.ExtendedStorageService;
import com.tesis.aether.core.services.storage.constants.StorageServiceConstants;
import com.tesis.aether.core.services.storage.object.StorageObject;
import com.tesis.aether.core.services.storage.object.StorageObjectMetadata;
import com.tesis.aether.core.services.storage.object.constants.StorageObjectConstants;

public abstract class StorageServiceTest {

	private ExtendedStorageService service;

	@BeforeClass
	public void initialize() {		
		service = getStorageService();
		try {
			service.connect(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public void finish() {		
		service = getStorageService();
		try {
			service.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	@BeforeMethod
	public void initializeMethod() {
		
		try {
			FileUtils.forceMkdir(new File("resources/TEST_FOLDER/"));
			new File("resources/test.1").createNewFile();
			new File("resources/TEST_FOLDER/test.2").createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}

	@AfterMethod
	public void cleanUpAfterMethod() {
		try {
			service.delete("resources", true);
			service.delete("resources1", true);
		} catch (Exception e) {
		}
		
		FileUtils.deleteQuietly(new File("resources/"));
	}
	
	@Test
	public void checkDirectoryExistsTest() {
				
		try {
			assert !service.checkDirectoryExists("resources/TEST_FOLDER/");
			service.createFolder("resources/TEST_FOLDER/");
			assert service.checkDirectoryExists("resources/TEST_FOLDER/");
		} catch(Exception e) {
			assert false;
		}
	}
	
	@Test
	public void checkFileExistsTest() {
				
		try {
			assert !service.checkFileExists("resources/TEST_FOLDER/test.2");
			service.uploadSingleFile(new File("resources/TEST_FOLDER/test.2"), "resources/TEST_FOLDER/");
			assert service.checkFileExists("resources/TEST_FOLDER/test.2");
		} catch(Exception e) {
			assert false;
		}
	}

	@Test
	public void checkObjectExistsTest() {
				
		try {
			assert !service.checkObjectExists("resources/TEST_FOLDER/test.2");
			assert !service.checkObjectExists("resources/TEST_FOLDER/");
			service.uploadSingleFile(new File("resources/TEST_FOLDER/test.2"), "resources/TEST_FOLDER/");
			assert service.checkObjectExists("resources/TEST_FOLDER/test.2");
			assert service.checkObjectExists("resources/TEST_FOLDER/");
		} catch(Exception e) {
			assert false;
		}
	}

	@Test
	public void copyFileTest() {

		try {
			assert !service.checkObjectExists("resources1");
			service.upload(new File("resources"), "");
			service.copyFile("resources", "resources1");
			assert service.checkObjectExists("resources1/resources/TEST_FOLDER/test.2");
			assert service.checkObjectExists("resources1/resources/test.1");
		} catch(Exception e) {
			assert false;
		}
	}

	@Test
	public void createFolderTest() {
				
		try {
			assert !service.checkDirectoryExists("resources/TEST_FOLDER/");
			service.createFolder("resources/TEST_FOLDER/");
			assert service.checkDirectoryExists("resources/TEST_FOLDER/");
		} catch(Exception e) {
			assert false;
		}
		
	}

	@Test
	public void deleteTest() {
				
		try {
			assert !service.checkObjectExists("resources/TEST_FOLDER/test.2");
			assert !service.checkObjectExists("resources/TEST_FOLDER/");
			
			service.uploadSingleFile(new File("resources/TEST_FOLDER/test.2"), "resources/TEST_FOLDER/");
			
			assert service.checkObjectExists("resources/TEST_FOLDER/test.2");
			assert service.checkObjectExists("resources/TEST_FOLDER/");
			
			service.delete("resources/TEST_FOLDER/", true);

			assert !service.checkObjectExists("resources/TEST_FOLDER/test.2");
			assert !service.checkObjectExists("resources/TEST_FOLDER/");
			
		} catch(Exception e) {
			assert false;
		}
		
	}

	@Test
	public void downloadDirectoryToDirectoryTest() {

		try {
			File localFile = new File("resources/Downloaded_1/");
			assert !localFile.exists();
			service.upload(new File("resources"), "");
			service.downloadDirectoryToDirectory("resources", localFile);
			assert new File("resources/Downloaded_1/resources/test.1").exists();
			assert new File("resources/Downloaded_1/resources/TEST_FOLDER/test.2").exists();
		} catch(Exception e) {
			assert false;
		}
		
	}

	@Test
	public void downloadFileToDirectoryTest() {
		try {
			File localFile = new File("resources/Downloaded_2/test.2");
			assert !localFile.exists();
			service.uploadSingleFile(new File("resources/TEST_FOLDER/test.2"), "resources/TEST_FOLDER/");
			service.downloadFileToDirectory("resources/TEST_FOLDER/test.2", new File("resources/Downloaded_2"));
			assert localFile.exists();
		} catch(Exception e) {
			assert false;
		}					
	}

	@Test
	public void downloadToDirectoryTest() {

		try {
			File localFile = new File("resources/Downloaded_1/");
			assert !localFile.exists();
			service.upload(new File("resources"), "");
			service.downloadToDirectory("resources", localFile);
			assert new File("resources/Downloaded_1/resources/test.1").exists();
			assert new File("resources/Downloaded_1/resources/TEST_FOLDER/test.2").exists();
		} catch(Exception e) {
			assert false;
		}
		
		try {
			File localFile = new File("resources/Downloaded_2/test.2");
			assert !localFile.exists();
			service.uploadSingleFile(new File("resources/TEST_FOLDER/test.2"), "resources/TEST_FOLDER/");
			service.downloadToDirectory("resources/TEST_FOLDER/test.2", new File("resources/Downloaded_2"));
			assert localFile.exists();
		} catch(Exception e) {
			assert false;
		}
		
	}

	@Test
	public void getInputStreamTest() {
		try {
			service.uploadSingleFile(new File("resources/TEST_FOLDER/test.2"), "resources/TEST_FOLDER/");
			InputStream inputStream = service.getInputStream("resources/TEST_FOLDER/test.2");
			assert inputStream != null;			
			inputStream.close();
		} catch(Exception e) {
			assert false;
		}				
	}

	@Test
	public void getMetadataForObjectTest() {
		try {
			service.uploadSingleFile(new File("resources/TEST_FOLDER/test.2"), "resources/TEST_FOLDER/");
			StorageObjectMetadata metadataForObject = service.getMetadataForObject("resources/TEST_FOLDER/test.2");
			assert metadataForObject != null;			
			assert metadataForObject.getLastModified() != null;
			assert metadataForObject.getLength() == 0;
			assert metadataForObject.getName().equals("test.2");
			assert metadataForObject.getPath().equals("resources/TEST_FOLDER");
			assert metadataForObject.getPathAndName().equals("resources/TEST_FOLDER/test.2");
			assert metadataForObject.getType().equals(StorageObjectConstants.FILE_TYPE);
			assert metadataForObject.getUri() != null;
		} catch(Exception e) {
			assert false;
		}						
	}

	@Test
	public void getPublicURLForPathTest() {
		try {
			service.uploadSingleFile(new File("resources/TEST_FOLDER/test.2"), "resources/TEST_FOLDER/");
			assert service.getPublicURLForPath("resources/TEST_FOLDER/test.2") != null;			
		} catch(Exception e) {
			assert false;
		}		
	}

	@Test
	public void getStorageObjectTest() {
		try {
			service.uploadSingleFile(new File("resources/TEST_FOLDER/test.2"), "resources/TEST_FOLDER/");
			StorageObject storageObject = service.getStorageObject("resources/TEST_FOLDER/test.2");
			assert storageObject != null;		
			assert storageObject.getMetadata() != null;
			assert storageObject.getStream() != null;	
			storageObject.getStream().close();
		} catch(Exception e) {
			assert false;
		}						
	}

	@Test
	public void lastModifiedTest() {
		try {
			service.uploadSingleFile(new File("resources/TEST_FOLDER/test.2"), "resources/TEST_FOLDER/");
			assert service.lastModified("resources/TEST_FOLDER/test.2") != null;			
		} catch(Exception e) {
			assert false;
		}
	}

	@Test
	public void listFilesTest() {
		
		try {
			assert !service.checkDirectoryExists("resources");
			service.uploadDirectory(new File("resources"), "");
			assert service.listFiles("resources", true).size() == 3;
		} catch(Exception e) {
			assert false;
		}
		
	}

	@Test
	public void migrateDataTest() {
		ServiceAccountProperties properties = new ServiceAccountProperties();
		String tempDirectory = System.getProperty("java.io.tmpdir") + "/REMOTE_MOCK_MIGRATE/";
		properties.putProperty(StorageServiceConstants.LOCAL_BASE_FOLDER, tempDirectory);
		
		ExtendedStorageService migrateService = new LocalStorageService();
		migrateService.setServiceProperties(properties);

		try {
			assert !service.checkObjectExists("resources");
			assert !migrateService.checkObjectExists("resources");
			service.upload(new File("resources"), "");
			service.migrateData("resources", migrateService, "");
			assert migrateService.checkObjectExists("resources/TEST_FOLDER/test.2");
			assert migrateService.checkObjectExists("resources/test.1");
			migrateService.delete("resources", true);
		} catch(Exception e) {
			assert false;
		}
		
		new File(tempDirectory).delete();
	}

	@Test
	public void moveFileTest() {

		try {
			assert !service.checkObjectExists("resources1");
			service.upload(new File("resources"), "");
			service.moveFile("resources", "resources1");
			assert !service.checkObjectExists("resources/TEST_FOLDER/test.2");
			assert !service.checkObjectExists("resources/test.1");
			assert !service.checkObjectExists("resources");
			assert service.checkObjectExists("resources1/resources/TEST_FOLDER/test.2");
			assert service.checkObjectExists("resources1/resources/test.1");
		} catch(Exception e) {
			assert false;
		}
		
	}

	@Test
	public void sizeOfTest() {
		try {
			service.uploadSingleFile(new File("resources/TEST_FOLDER/test.2"), "resources/TEST_FOLDER/");
			assert service.sizeOf("resources/TEST_FOLDER/test.2") == 0;			
		} catch(Exception e) {
			assert false;
		}		
	}

	@Test
	public void uploadTest() {		
		try {
			assert !service.checkFileExists("resources1/TEST_FOLDER/test.2");
			service.upload(new File("resources/TEST_FOLDER/test.2"), "resources1/TEST_FOLDER_2/");
			assert service.checkFileExists("resources1/TEST_FOLDER_2/test.2");
		} catch(Exception e) {
			assert false;
		}	

		try {
			assert !service.checkDirectoryExists("resources");
			service.upload(new File("resources"), "");
			assert service.checkFileExists("resources/test.1");
			assert service.checkFileExists("resources/TEST_FOLDER/test.2");
		} catch(Exception e) {
			assert false;
		}
	}

	@Test
	public void uploadDirectoryTest() {
				
		try {
			assert !service.checkDirectoryExists("resources");
			service.uploadDirectory(new File("resources"), "");
			assert service.checkFileExists("resources/test.1");
			assert service.checkFileExists("resources/TEST_FOLDER/test.2");
		} catch(Exception e) {
			assert false;
		}
		
	}

	@Test
	public void uploadSingleFileTest() {				
		try {
			assert !service.checkFileExists("resources/TEST_FOLDER/test.2");
			service.uploadSingleFile(new File("resources/TEST_FOLDER/test.2"), "resources/TEST_FOLDER/");
			assert service.checkFileExists("resources/TEST_FOLDER/test.2");
		} catch(Exception e) {
			assert false;
		}		
	}
	
	protected abstract ExtendedStorageService getStorageService();
	
}
package pl.psnc.indigo.fg.kepler;

import java.io.File;
import java.util.ArrayList;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.util.SingletonAttribute;
import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.BaseAPI;
import pl.psnc.indigo.fg.api.restful.jaxb.InputFile;
import pl.psnc.indigo.fg.api.restful.jaxb.Task;
import pl.psnc.indigo.fg.api.restful.jaxb.Upload;
import ptolemy.data.ArrayToken;

public class UploadFiles extends LimitedFiringSource {

  public TypedIOPort userPort;
  public TypedIOPort idPort;
  public TypedIOPort inputFilesPort;
  public TypedIOPort uploadURL;

  public UploadFiles(CompositeEntity container, String name)
    throws NameDuplicationException, IllegalActionException {
    super(container, name);

    userPort = new TypedIOPort(this, "user", true, false);
    new SingletonAttribute(userPort, "_showName");
    userPort.setTypeEquals(BaseType.STRING);

    idPort = new TypedIOPort(this, "id", true, false);
    new SingletonAttribute(idPort, "_showName");
    idPort.setTypeEquals(BaseType.STRING);

    inputFilesPort = new TypedIOPort(this, "inputFiles", true, false);
    new SingletonAttribute(inputFilesPort, "_showName");
    inputFilesPort.setTypeEquals(BaseType.GENERAL);

    uploadURL = new TypedIOPort(this, "uploadURL", true, false);
    new SingletonAttribute(uploadURL, "_showName");
    uploadURL.setTypeEquals(BaseType.STRING);

    output.setTypeEquals(BaseType.STRING);
  }

  @Override
  public void fire() throws IllegalActionException {
    super.fire();

    String userString = null;
    String idString = null;
    String file = null;
    String strURL = null;

    if (userPort.getWidth() > 0) {
      StringToken userToken = (StringToken) userPort.get(0);
      userString = userToken.stringValue();
    }

    if (idPort.getWidth() > 0) {
      StringToken idToken = (StringToken) idPort.get(0);
      idString = idToken.stringValue();
    }

    ArrayList<String> inputFilesArray = new ArrayList();

    if (inputFilesPort.getWidth() > 0) {
      ArrayToken inputFilesToken = (ArrayToken) inputFilesPort.get(0);
      for (int i = 0; i < inputFilesToken.length(); i++) {
        StringToken arrayElement = (StringToken) inputFilesToken.getElement(i);
        inputFilesArray.add(arrayElement.stringValue());
      }
    }

    if (uploadURL.getWidth() > 0) {
      StringToken inputToken = (StringToken) uploadURL.get(0);
      strURL = inputToken.stringValue();
    }

    TasksAPI restAPI = new TasksAPI(BaseAPI.LOCALHOST_ADDRESS);

    try {
      for (int i = 0; i < inputFilesArray.size(); i++) {
        Task prepareToSubmit = new Task();
        prepareToSubmit.setUser(userString);
        prepareToSubmit.setId(idString);
        Upload result = restAPI.uploadFileForTask(prepareToSubmit, strURL, new File(inputFilesArray.get(i)));
        output.send(0, new StringToken(result.getTask()));
      }
    } catch (Exception ex) {
      throw new IllegalActionException("There was an issue while uploading file.");
    }
  }
}

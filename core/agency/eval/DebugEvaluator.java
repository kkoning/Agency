package agency.eval;

import agency.AgentModel;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.UUID;
import java.util.jar.Pack200;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DebugEvaluator implements Evaluator {
public static final long serialVersionUID = 1L;

@Override
public Stream<EvaluationGroup> evaluate(Stream<EvaluationGroup> evaluationGroups) {
  List<EvaluationGroup> groups = evaluationGroups.collect(Collectors.toList());
  EvaluationGroup first = groups.get(0);

  try {
    File outFile = new File("debug-gen" + first.generation + ".zip");
    FileOutputStream fos = new FileOutputStream(outFile);
    ZipOutputStream zos = new ZipOutputStream(fos);
    for (EvaluationGroup eg : groups) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream ps = new PrintStream(baos);
      AgentModel model = eg.getModel();
      model.enableDebug(ps);
      eg.run();
      ps.flush();
      ps.close();
      baos.close();
      byte[] data = baos.toByteArray();
      UUID modelID = eg.getId();
      ZipEntry e = new ZipEntry(modelID.toString() + ".debug.txt");
      zos.putNextEntry(e);
      zos.write(data);
      zos.closeEntry();
    }
    zos.flush();
    zos.close();

  } catch (Exception e) {
    throw new RuntimeException(e);
  }
  return groups.stream();
}

@Override
public void close() {
  // Unnecessary
}


@Override
public void readXMLConfig(Element e) {
  // No configuration required
}

@Override
public void writeXMLConfig(Element e) {
  // No configuration required
}

@Override
public void resumeFromCheckpoint() {
  // No configuration needed
}

}

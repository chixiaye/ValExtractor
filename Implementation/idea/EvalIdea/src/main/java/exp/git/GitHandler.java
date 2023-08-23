package exp.git;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.io.IOException;
public class GitHandler {
    Git fGit;
    String fPath;
    public GitHandler(String path) throws IOException {
        fPath=path;
    }
    void open() throws IOException {
        fGit = Git.open(new File(fPath));
    }

    void close(){
        fGit.close();
    }

    public void reset() throws Exception{
        fGit.reset().setMode(ResetCommand.ResetType.HARD).call();
    }

    public void restoreCode( ) {
        Git git = null;
        try {
            git = Git.open(new File(fPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            git.reset().setMode(ResetCommand.ResetType.HARD).call();
        } catch (RefAlreadyExistsException e) {
            e.printStackTrace();
        } catch (RefNotFoundException e) {
            e.printStackTrace();
        } catch (InvalidRefNameException e) {
            e.printStackTrace();
        } catch (CheckoutConflictException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        git.close();
        System.out.println("restore!\n");
    }

    public void checkout(String commitSHA) {
        Repository repository = fGit.getRepository();
        try {
            System.out.println(commitSHA);
            fGit.checkout().setForced(true).setName(commitSHA).call();
        } catch (CheckoutConflictException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        repository.close();
    }

    public void removeLock() {
        File file=new File(fPath+"\\.git\\index");
        file.deleteOnExit();
    }
}

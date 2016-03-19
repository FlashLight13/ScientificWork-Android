package stydying.algo.com.algostudying.network.interfaces;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import stydying.algo.com.algostudying.data.entities.tasks.Task;
import stydying.algo.com.algostudying.data.entities.tasks.TaskGroup;

/**
 * Created by Anton on 19.03.2016.
 */
public interface TaskInterface {

    @POST("/updateTask")
    Call<Task> updateTask(@Body UpdateTaskData data);

    @POST("/createOrUpdateWorld")
    Call<WorldData> createUpdateTask(@Body WorldData worldData);

    @POST("/getTaskGroupsNames")
    Call<List<TaskGroup>> getTaskGroupsNames();

    @POST("removeTaskGroup")
    Call<Object> removeTaskGroup(@Body String jsonId);

    @POST("/task")
    Call<Task> getTask(@Body String jsonId);

    @POST("updateTaskGroup")
    Call<TaskGroup> updateTaskGroup(@Body UpdateTaskGroupData data);

    final class UpdateTaskGroupData {
        public String title;
        public Long id;
        public List<String> userIds;

        public UpdateTaskGroupData() {
            super();
        }

        public UpdateTaskGroupData(String title, Long id, List<String> userIds) {
            super();
            this.title = title;
            this.id = id;
            this.userIds = userIds;
        }
    }

    final class UpdateTaskData {
        public Task updatedTask;
        public long taskGroupId;
        public List<String> usersIds;

        public UpdateTaskData() {
            super();
        }

        public UpdateTaskData(Task updatedTask, long taskGroupId, List<String> usersIds) {
            super();
            this.updatedTask = updatedTask;
            this.taskGroupId = taskGroupId;
            this.usersIds = usersIds;
        }
    }

    final class WorldData {
        public TaskGroup taskGroup;
        public Task task;

        public WorldData() {
        }

        public WorldData(TaskGroup taskGroup, Task task) {
            super();
            this.taskGroup = taskGroup;
            this.task = task;
        }
    }
}

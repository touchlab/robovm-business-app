package org.robovm.samples.contractr.core.service;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;
import org.robovm.samples.contractr.core.common.OrmliteOpenHelper;
import org.robovm.samples.contractr.core.common.RoboVMContext;
import org.robovm.samples.contractr.core.common.SQLiteException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by kgalligan on 2/14/16.
 */
public class DatabaseHelper extends OrmliteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    static Class[] managedClasses = new Class[]{Client.class, Task.class, WorkUnit.class};

    private final MBassador<Object> clientBus;
    private final MBassador<Object> taskBus;

    private Client selectedClient;
    private Task selectedTask;

    public DatabaseHelper(RoboVMContext context, String databaseName, MBassador<Object> clientBus, MBassador<Object> taskBus) {
        super(context, databaseName, DATABASE_VERSION, managedClasses);
        this.clientBus = clientBus;
        this.taskBus = taskBus;
        subscribeClient(this);
    }

    @Override
    public void onCreate(Connection db, ConnectionSource connectionSource) {
        try {
            for (Class managedClass : managedClasses) {
                TableUtils.createTableIfNotExists(connectionSource, managedClass);
            }
        } catch (SQLException e) {
            throw new SQLiteException(e);
        }
    }

    @Override
    public void onUpgrade(Connection db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            for (int i = managedClasses.length - 1; i >= 0; i--) {
                TableUtils.dropTable(connectionSource, managedClasses[i], true);
            }
        } catch (SQLException e) {
            throw new SQLiteException(e);
        }
    }

    public void subscribeClient(Object listener) {
        clientBus.subscribe(listener);
    }

    public void unsubscribeClient(Object listener) {
        clientBus.unsubscribe(listener);
    }

    public void subscribeTask(Object listener) {
        taskBus.subscribe(listener);
    }

    public void unsubscribeTask(Object listener) {
        taskBus.unsubscribe(listener);
    }

    private Dao<Client, Integer> getClientDao()
    {
        try {
            return getDao(Client.class);
        } catch (SQLException e) {
            throw new SQLiteException(e);
        }
    }

    private Dao<Task, Integer> getTaskDao()
    {
        try {
            return getDao(Task.class);
        } catch (SQLException e) {
            throw new SQLiteException(e);
        }
    }

    private Dao<WorkUnit, Integer> getWorkUnitDao()
    {
        try {
            return getDao(WorkUnit.class);
        } catch (SQLException e) {
            throw new SQLiteException(e);
        }
    }

    public Client getClientAt(int pos) {
        try {
            return getClientDao().queryForAll().get(pos);
        } catch (SQLException e) {
            throw new SQLiteException(e);
        }
    }

    public int getClientCount()
    {
        try {
            return (int)getClientDao().countOf();
        } catch (SQLException e) {
            throw new SQLiteException(e);
        }
    }

    public List<Client> getAllClients() {
        try {
            return getClientDao().queryForAll();
        } catch (SQLException e) {
            throw new SQLiteException(e);
        }
    }

    public int getTaskCountUnfinished()
    {
        List<Task> tasks;
        try {
            tasks = getTaskDao().queryForAll();
        } catch (SQLException e) {
            throw new SQLiteException(e);
        }

        List<Task> filtered = new ArrayList<>();
        for (Task task : tasks) {
            if(!task.finished)
            {
                filtered.add(task);
            }
        }

        return filtered.size();
    }

    public List<Task> getTasksForClient(Client client)
    {
        try {
            QueryBuilder<Task, Integer> queryBuilder = getTaskDao().queryBuilder();
            Where<Task, Integer> where = queryBuilder.where();
            return where.eq("client_id", client.id).query();
        } catch (SQLException e) {
            throw new SQLiteException(e);
        }
    }

    public void saveTask(Task task)
    {
        if(task.id == null)
        {
            task.id = UUID.randomUUID().toString();
        }

        try {
            getTaskDao().createOrUpdate(task);
            taskBus.publish(new TaskSavedEvent(task));
        } catch (SQLException e) {
            throw new SQLiteException(e);
        }
    }

    public void saveClient(Client client) {
        if (client.id == null) {
            // New client
            client.id = UUID.randomUUID().toString();
        }

        try {
            getClientDao().createOrUpdate(client);
            clientBus.publish(new ClientSavedEvent(client));
        } catch (SQLException e) {
            throw new SQLiteException(e);
        }
        /*try {
            Connection conn = getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, client.getName());
                stmt.setString(2, client.getHourlyRate().toString());
                stmt.setString(3, id);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dirty = true;
            if (taskManager != null) {
                taskManager.dirty = true;
            }
        }*/
    }


    public Task getWorkingTask() {
        try {
            List<Task> tasks = getTaskDao().queryForAll();

            for (Task task : tasks) {
                if(task.workStartTime != null)
                    return task;
            }

            return null;
        } catch (SQLException e) {
            throw new SQLiteException(e);
        }
    }

    public void startWork(Task task) {
        try {
            if (task.workStartTime != null) {
                // Already working on this task
                return;
            }
            stopWork();
            task.workStartTime = (new Date());
            taskBus.publish(new WorkStartedEvent(task));

            getTaskDao().createOrUpdate(task);
        } catch (SQLException e) {
            throw new SQLiteException(e);
        }
    }

    public void delete(Client client) {
        try {
            int deleteCount = getClientDao().delete(client);
            if (deleteCount > 0) {
                if (selectedClient != null && client.id.equals(selectedClient.id)) {
                    selectClient(null);
                }
                clientBus.publish(new ClientDeletedEvent(client));
            }
        } catch (SQLException e) {
            throw new SQLiteException(e);
        }
    }

    public void selectClient(Client newClient) {
        Client oldClient = this.selectedClient;
        this.selectedClient = newClient;
        if (!oldClient.id.equals(newClient.id)) {
            clientBus.publish(new SelectedClientChangedEvent(oldClient, newClient));
        }
    }

    @Handler
    public void selectedClientChanged(SelectedClientChangedEvent event) {
        selectTask(null);
    }

    public void selectTask(Task newTask) {
        Task oldTask = this.selectedTask;
        this.selectedTask = newTask;
        if (!oldTask.id.equals(newTask.id)) {
            taskBus.publish(new SelectedTaskChangedEvent(oldTask, newTask));
        }
    }



    /**
     * Deletes the specified {@link Task} from the underlying storage. Fires
     * {@link TaskDeletedEvent} if the {@link Task} existed in the storage and
     * was deleted. Also fires {@link SelectedTaskChangedEvent} if the selected
     * {@link Task} is deleted.
     */
    public void delete(Task task) {
        try {
            int deleteCount = getTaskDao().delete(task);
            if (deleteCount > 0) {
                if (task.equals(selectedTask)) {
                    selectTask(null);
                }
                taskBus.publish(new TaskDeletedEvent(task));
            }
        } catch (SQLException e) {
            throw new SQLiteException(e);
        }
    }



    /**
     * Stops work on the {@link Task} currently being worked on.
     */
    public void stopWork() {
        stopWork(System.currentTimeMillis());
    }

    protected void stopWork(long now) {
        try {
            Task workingTask = getWorkingTask();
            if (workingTask == null) {
                return;
            }
            Date startTime = workingTask.workStartTime;
            Date endTime = new Date(now);
            workingTask.secondsWorked = ((int) (workingTask.secondsWorked
                    + (endTime.getTime() - startTime.getTime()) / 1000));
            workingTask.workStartTime = (null);
            WorkUnit workUnit = new WorkUnit();
            workUnit.startTime = startTime;
            workUnit.endTime = endTime;
            workUnit.id = UUID.randomUUID().toString();
            workUnit.task = workingTask;

            getTaskDao().createOrUpdate(workingTask);
            getWorkUnitDao().createOrUpdate(workUnit);

            taskBus.publish(new WorkStoppedEvent(workingTask));
        } catch (SQLException e) {
            throw new SQLiteException(e);
        }
    }

    /**
     * Event fired when the selected {@link Client} changes.
     */
    public static class SelectedClientChangedEvent {
        private final Client oldClient;
        private final Client newClient;

        SelectedClientChangedEvent(Client oldClient, Client newClient) {
            this.oldClient = oldClient;
            this.newClient = newClient;
        }

        public Client getOldClient() {
            return oldClient;
        }

        public Client getNewClient() {
            return newClient;
        }
    }

    /**
     * Event fired when a {@link Client} has been saved.
     */
    public static class ClientSavedEvent {
        private final Client client;

        ClientSavedEvent(Client client) {
            this.client = client;
        }

        public Client getClient() {
            return client;
        }
    }

    /**
     * Event fired when a {@link Client} has been deleted.
     */
    public static class ClientDeletedEvent {
        private final Client client;

        ClientDeletedEvent(Client client) {
            this.client = client;
        }

        public Client getClient() {
            return client;
        }
    }

    /**
     * Event fired when the selected {@link Task} changes.
     */
    public static class SelectedTaskChangedEvent {
        private final Task oldTask;
        private final Task newTask;

        SelectedTaskChangedEvent(Task oldTask, Task newTask) {
            this.oldTask = oldTask;
            this.newTask = newTask;
        }

        public Task getOldTask() {
            return oldTask;
        }

        public Task getNewTask() {
            return newTask;
        }
    }

    /**
     * Event fired when a {@link Task} has been saved.
     */
    public static class TaskSavedEvent {
        private final Task task;

        TaskSavedEvent(Task task) {
            this.task = task;
        }

        public Task getTask() {
            return task;
        }
    }

    /**
     * Event fired when a {@link Task} has been deleted.
     */
    public static class TaskDeletedEvent {
        private final Task task;

        TaskDeletedEvent(Task task) {
            this.task = task;
        }

        public Task getTask() {
            return task;
        }
    }

    /**
     * Event fired when work is started on a {@link Task}.
     */
    public static class WorkStartedEvent {
        private final Task task;

        WorkStartedEvent(Task task) {
            this.task = task;
        }

        public Task getTask() {
            return task;
        }
    }

    /**
     * Event fired when work is stopped on a {@link Task}.
     */
    public static class WorkStoppedEvent {
        private final Task task;

        WorkStoppedEvent(Task task) {
            this.task = task;
        }

        public Task getTask() {
            return task;
        }
    }
}

/*
 * Copyright © 2020 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.plugin.gcp.firestore.sink;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.util.Map;

/**
 * An OutputFormat that sends the output of a Hadoop job to the Firestore. FirestoreOutputFormat accepts key,
 * value pairs, but the returned {@link FirestoreRecordWriter} writes only the value to the database as each
 * Firestore entity already contains a Datastore key.
 */
public class FirestoreOutputFormat extends OutputFormat<NullWritable, Map<String, Object>> {

  @Override
  public RecordWriter<NullWritable, Map<String, Object>> getRecordWriter(TaskAttemptContext taskAttemptContext) {
    return new FirestoreRecordWriter(taskAttemptContext);
  }

  @Override
  public void checkOutputSpecs(JobContext jobContext) {
    //no-op
  }

  /**
   * No op output committer.
   */
  @Override
  public OutputCommitter getOutputCommitter(TaskAttemptContext taskAttemptContext) {
    return new OutputCommitter() {
      @Override
      public void setupJob(JobContext jobContext) {

      }

      @Override
      public void setupTask(TaskAttemptContext taskAttemptContext) {

      }

      @Override
      public boolean needsTaskCommit(TaskAttemptContext taskAttemptContext) {
        return false;
      }

      @Override
      public void commitTask(TaskAttemptContext taskAttemptContext) {

      }

      @Override
      public void abortTask(TaskAttemptContext taskAttemptContext) {

      }
    };
  }
}

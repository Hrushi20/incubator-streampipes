/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.apache.streampipes.manager.preview;

import org.apache.streampipes.manager.data.PipelineGraph;
import org.apache.streampipes.manager.data.PipelineGraphBuilder;
import org.apache.streampipes.manager.execution.http.HttpRequestBuilder;
import org.apache.streampipes.manager.matching.InvocationGraphBuilder;
import org.apache.streampipes.manager.operations.Operations;
import org.apache.streampipes.model.SpDataSet;
import org.apache.streampipes.model.SpDataStream;
import org.apache.streampipes.model.base.InvocableStreamPipesEntity;
import org.apache.streampipes.model.base.NamedStreamPipesEntity;
import org.apache.streampipes.model.graph.DataProcessorInvocation;
import org.apache.streampipes.model.pipeline.Pipeline;
import org.apache.streampipes.model.preview.PipelinePreviewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class PipelinePreview {

  public PipelinePreviewModel initiatePreview(Pipeline pipeline) {
    String previewId = generatePreviewId();
    pipeline.setActions(new ArrayList<>());
    PipelineGraph pipelineGraph = new PipelineGraphBuilder(pipeline).buildGraph();
    List<NamedStreamPipesEntity> graphs = new ArrayList<>(new InvocationGraphBuilder(pipelineGraph, previewId).buildGraphs());
    graphs.addAll(pipeline.getStreams().stream().filter(stream -> !(stream instanceof SpDataSet)).collect(Collectors.toList()));

    invokeGraphs(filter(graphs));
    storeGraphs(previewId, graphs);

    return makePreviewModel(previewId, graphs);
  }

  public void deletePreview(String previewId) {
    List<NamedStreamPipesEntity> graphs = ActivePipelinePreviews.INSTANCE.getInvocationGraphs(previewId);
    detachGraphs(filter(graphs));
    deleteGraphs(previewId);
  }

  public String getPipelineElementPreview(String previewId,
                                          String pipelineElementDomId) throws IllegalArgumentException {
    Optional<NamedStreamPipesEntity> graphOpt = ActivePipelinePreviews
            .INSTANCE
            .getInvocationGraphForPipelineELement(previewId, pipelineElementDomId);

    if (graphOpt.isPresent()) {
      NamedStreamPipesEntity graph = graphOpt.get();
      if (graph instanceof DataProcessorInvocation) {
        return Operations.getRuntimeInfo(((DataProcessorInvocation) graph).getOutputStream());
      } else if (graph instanceof SpDataStream) {
        return Operations.getRuntimeInfo((SpDataStream) graph);
      } else {
        throw new IllegalArgumentException("Requested pipeline element is not a data processor");
      }
    } else {
      throw new IllegalArgumentException("Could not find pipeline element");
    }
  }

  private void invokeGraphs(List<InvocableStreamPipesEntity> graphs) {
    graphs.forEach(g -> new HttpRequestBuilder(g, g.getBelongsTo(), null).invoke());
  }

  private void detachGraphs(List<InvocableStreamPipesEntity> graphs) {
    graphs.forEach(g -> new HttpRequestBuilder(g, g.getUri(), null).detach());
  }

  private void deleteGraphs(String previewId) {
    ActivePipelinePreviews.INSTANCE.removePreview(previewId);
  }

  private void storeGraphs(String previewId,
                           List<NamedStreamPipesEntity> graphs) {
    ActivePipelinePreviews.INSTANCE.addActivePreview(previewId, graphs);
  }

  private String generatePreviewId() {
    return UUID.randomUUID().toString();
  }

  private PipelinePreviewModel makePreviewModel(String previewId,
                                                List<NamedStreamPipesEntity> graphs) {
    PipelinePreviewModel previewModel = new PipelinePreviewModel();
    previewModel.setPreviewId(previewId);
    previewModel.setSupportedPipelineElementDomIds(collectDomIds(graphs));

    return previewModel;
  }

  private List<String> collectDomIds(List<NamedStreamPipesEntity> graphs) {
    return graphs
            .stream()
            .map(NamedStreamPipesEntity::getDOM)
            .collect(Collectors.toList());
  }

  private List<InvocableStreamPipesEntity> filter(List<NamedStreamPipesEntity> graphs) {
    List<InvocableStreamPipesEntity> dataProcessors = new ArrayList<>();
    graphs.stream()
            .filter(g -> g instanceof DataProcessorInvocation)
            .forEach(p -> dataProcessors.add((DataProcessorInvocation) p));

    return dataProcessors;
  }
}

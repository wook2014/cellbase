/*
 * Copyright 2015-2020 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.cellbase.lib.managers;

import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.GenomeSequenceFeature;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.core.TranscriptTfbs;
import org.opencb.cellbase.lib.impl.core.CellBaseCoreDBAdaptor;
import org.opencb.cellbase.core.api.queries.GeneQuery;
import org.opencb.cellbase.core.api.queries.ProjectionQueryOptions;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.impl.core.GeneMongoDBAdaptor;
import org.opencb.cellbase.lib.impl.core.GenomeMongoDBAdaptor;

import java.util.ArrayList;
import java.util.List;

public class GeneManager extends AbstractManager implements AggregationApi<GeneQuery, Gene> {

    private GeneMongoDBAdaptor geneDBAdaptor;
    private GenomeMongoDBAdaptor genomeDBAdaptor;

    public GeneManager(String species, String assembly, CellBaseConfiguration configuration) {
        super(species, assembly, configuration);
        this.init();
    }

    private void init() {
        geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(species, assembly);
        genomeDBAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(species, assembly);
    }

    @Override
    public CellBaseCoreDBAdaptor<GeneQuery, Gene> getDBAdaptor() {
        return geneDBAdaptor;
    }

    public List<CellBaseDataResult<Gene>> info(List<String> ids, ProjectionQueryOptions query, String source) {
        return geneDBAdaptor.info(ids, query, source);
    }

    public CellBaseDataResult<GenomeSequenceFeature> getSequence(GeneQuery query) {
        // get the coordinates for the gene
        CellBaseDataResult<Gene> geneCellBaseDataResult = geneDBAdaptor.query(query);
        // get the sequences for those coordinates
        if (geneCellBaseDataResult.getNumResults() > 0) {
            List<Gene> results = geneCellBaseDataResult.getResults();
            Gene gene = results.get(0);
            Region region = new Region(gene.getChromosome(), gene.getStart(), gene.getEnd());
            return genomeDBAdaptor.getSequence(region, query.toQueryOptions());
        }
        return null;
    }

    public List<CellBaseDataResult<GenomeSequenceFeature>> getSequence(List<GeneQuery> queries) {
        List<CellBaseDataResult<GenomeSequenceFeature>> sequences = new ArrayList<>();
        for (GeneQuery query : queries) {
            sequences.add(getSequence(query));
        }
        return sequences;
    }

    public List<CellBaseDataResult<TranscriptTfbs>> getTfbs(GeneQuery query) {
        List<CellBaseDataResult<TranscriptTfbs>> geneQueryResults = new ArrayList<>();
        for (String gene : query.getIds()) {
            CellBaseDataResult<TranscriptTfbs> geneQueryResult = geneDBAdaptor.getTfbs(gene, query.toQueryOptions());
            geneQueryResult.setId(gene);
            geneQueryResults.add(geneQueryResult);
        }
        return geneQueryResults;
    }

}

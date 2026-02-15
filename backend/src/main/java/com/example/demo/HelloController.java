package com.example.demo;

import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.opensearch.client.opensearch.OpenSearchClient;


@RestController
public class HelloController {

    private final OpenSearchClient client;

    public HelloController(OpenSearchClient client) {

        this.client = client;
    }


    @GetMapping("/search")
    public List<job> search(
            @RequestParam String q,
            @RequestParam(defaultValue ="0") int page,
            @RequestParam(defaultValue = "10") int size) {
        System.out.println("SEARCH endpoint hit with query: " + q);
        List<job> jobs = getCandidatesWithFallback(q);

        List<job> ranked = jobs.stream()
                .map(j -> new AbstractMap.SimpleEntry<>(j,calculateScore(j,q)))
                .filter(e->e.getValue() >0)
                .sorted((a,b) -> b.getValue() -a.getValue())
                .map(AbstractMap.SimpleEntry::getKey)
                .toList();
        int start = page * size;
        int end = Math.min(start + size, ranked.size());

        if(start >= ranked.size()) {
            return List.of();
        }

        return ranked.subList(start,end);


    }


    private List<job> loadjobs() {
        System.out.println("Loading jobs FROM CSV");

        List<job> jobs = new ArrayList<>();
        int count = 0;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        getClass().getResourceAsStream("/jobs.csv")))) {

            String line;

            // Skip header
            br.readLine();

            while ((line = br.readLine()) != null) {

                // Split by pipe
                String[] parts = line.split("\\|");

                // We need at least: title | skills... | min | max
                if (parts.length < 4) {
                    continue;
                }

                String title = parts[0].trim();

                // Salary is ALWAYS the last two columns
                String minStr = parts[parts.length - 2].trim();
                String maxStr = parts[parts.length - 1].trim();

                // Join everything in the middle as skills/description
                StringBuilder skillsBuilder = new StringBuilder();
                for (int i = 1; i < parts.length - 2; i++) {
                    if (i > 1) skillsBuilder.append(" ");
                    skillsBuilder.append(parts[i]);
                }
                String skills = skillsBuilder.toString();

                double minSalary;
                double maxSalary;

                try {
                    minSalary = Double.parseDouble(minStr);
                    maxSalary = Double.parseDouble(maxStr);
                } catch (Exception ex) {
                    // Skip rows where salary is broken text
                    continue;
                }

                jobs.add(new job(title, skills, minSalary, maxSalary));
                count++;

                if (count % 5000 == 0) {
                    System.out.println("Loaded rows so far: " + count);
                }
            }

            System.out.println("Total rows loaded into memory: " + count);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jobs;
    }


    private int calculateScore(job job, String q) {
        int score = 0;
        String query = q.toLowerCase();
        String title = job.getTitle().toLowerCase();
        String skills = job.getSkills().toLowerCase();
        boolean titleMatch = title.contains(query);
        boolean strongSkillMatch =
                skills.contains(query + "developer") ||
                skills.contains(query + "engineer")||
                skills.contains("experience with" +query);


        if(query.isEmpty()) return 0;
        String[] tokens = query.split("\\s+");
        boolean matched = false;

        for (String token : tokens) {
            if(token.isBlank()) continue;

            if (title.contains(token)){
                score +=50;
                matched = true;
            }
            if (skills.contains(token)){
                score+= 20;
                matched = true;
            }
        }


        if (matched) score+= job.getMaxSalary();
        return score;
    }





    @PostMapping("/index")
    public String indexJobs() throws Exception {

        List<job> jobs = loadjobs();
        int ok = 0;
        int bad = 0;

        for (int i = 0; i < jobs.size(); i++) {
            job doc = jobs.get(i);
            final String id = String.valueOf(i + 1);
            try {
                client.index(req -> req
                        .index("jobs")
                        .id(id)
                        .document(doc)
                );
                ok++;

                if (ok % 1000 == 0) {
                    System.out.println("Indexed " + ok);
                }
            } catch (Exception e) {
                bad++;

            }
        }

        return "Indexed OK = " + ok + " bad =" +bad + "totalloaded =" + jobs.size();
    }


    private List<job> getCandidatesFromOpenSearch(String q) throws Exception {

        System.out.println("Fetching candidates FROM OPENSEARCH");

        SearchResponse<job> response = client.search(s -> s
                        .index("jobs")
                        .size(50)
                        .query(qb -> qb.bool(b -> b
                                .should(sh -> sh.match(m -> m
                                        .field("title")
                                        .query(FieldValue.of(q))
                                        .boost(3.0f)
                                ))
                                .should(sh -> sh.match(m -> m
                                        .field("skills")
                                        .query(FieldValue.of(q))
                                ))
                                .minimumShouldMatch("1")
                        )),
                job.class
        );

        List<job> results = new ArrayList<>();
        for (Hit<job> hit : response.hits().hits()) {
            if (hit.source() != null) {
                results.add(hit.source());
            }
        }

        return results;
    }

    private List<job> getCandidatesWithFallback(String q) {
        System.out.println("Trying OpenSearch first...");

        try{
            return getCandidatesFromOpenSearch(q);

        }catch (Exception e){
            System.out.println("OpenSearch failed, falling back ti CSV. Reason:" +e.getMessage());
            return loadjobs();
        }
    }
}

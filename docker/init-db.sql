-- Create databases for Confluence and Jira
-- These will be created when the Postgres container starts for the first time

CREATE DATABASE confluencedb WITH ENCODING 'UTF8';
CREATE DATABASE jiradb WITH ENCODING 'UTF8';

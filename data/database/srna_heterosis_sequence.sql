CREATE DATABASE srna_heterosis

CREATE TABLE IF NOT EXISTS srna_mapping_status (
  sequence_id int(11) NOT NULL,
  mapping_count int(10) unsigned NOT NULL,
  UNIQUE KEY sequence_id (sequence_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;

CREATE TABLE IF NOT EXISTS srna_sequence (
  sequence_id int(10) unsigned NOT NULL,
  sequence varchar(40) COLLATE latin1_general_ci NOT NULL,
  length tinyint(3) unsigned NOT NULL,
  UNIQUE KEY sequence_id (sequence_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;


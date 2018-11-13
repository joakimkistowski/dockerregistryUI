package persistence

import (
	"log"

	"github.com/jinzhu/gorm"

	// Using SQLite by default.
	_ "github.com/jinzhu/gorm/dialects/sqlite"
)

// DBConfig Configuration settings for persistence access.
type DBConfig struct {
	initialized  bool
	DBPathPrefix string
	DBName       string
	DBType       string
}

// DBHandle Handle for accessing the persistence context.
type DBHandle struct {
	db     *gorm.DB
	Config DBConfig
}

// NewDBConfig Creates a new DBConfig with default values.
func NewDBConfig() DBConfig {
	return DBConfig{initialized: true, DBPathPrefix: "/data/", DBName: "registryui.db", DBType: "sqlite3"}
}

// StartPersistenceContext Starts a persistence context and returns the handle to that context.
func StartPersistenceContext(config DBConfig) *DBHandle {
	config.initIfNecessary()
	db, err := gorm.Open(config.DBType, config.DBPathPrefix+config.DBName)
	if err != nil {
		log.Fatalln("Failed to connect database: " + config.DBType + " @ " + config.DBPathPrefix + config.DBName)
		return nil
	}
	db.AutoMigrate(&ImageCategory{})
	db.AutoMigrate(&ImageDescription{})
	db.AutoMigrate(&HelloMessage{})
	log.Println("Database connected: " + config.DBType + " @ " + config.DBPathPrefix + config.DBName)
	return &DBHandle{db: db, Config: config}
}

// StopPersistenceContext Stops the context managed using the handle.
func (handle *DBHandle) StopPersistenceContext() {
	handle.db.Close()
}

func (config *DBConfig) initIfNecessary() {
	if !config.initialized {
		*config = NewDBConfig()
	}
}

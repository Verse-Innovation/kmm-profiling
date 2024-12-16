package io.verse.profiling.adapter

import io.verse.profiling.analyzer.Analyzable
import io.verse.profiling.logger.Loggable
import io.verse.profiling.tracer.Traceable

interface ProfileableSubject : Traceable, Analyzable, Loggable